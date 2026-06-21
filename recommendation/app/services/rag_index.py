import logging
import threading
from dataclasses import dataclass

import faiss
import numpy as np
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.entities.course_chunk import CourseChunk
from app.entities.knowledge import KnowledgeChunk
from app.services.embedding_model import embed_documents


logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class IndexedChunk:
    id: str
    source_type: str
    content: str
    course_id: str
    lesson_ids: list[str]
    section_title: str | None = None
    document_id: str | None = None
    document_title: str | None = None
    source_url: str | None = None


class RAGIndex:
    def __init__(self):
        self._lock = threading.RLock()
        self._index = None
        self._chunks: list[IndexedChunk] = []
        self._dimension = 0

    def rebuild(self, db: Session) -> None:
        course_chunks = db.execute(select(CourseChunk)).scalars().all()
        knowledge_chunks = db.execute(select(KnowledgeChunk)).scalars().all()
        self._refresh_embeddings_if_needed(db, course_chunks, knowledge_chunks)
        indexed_chunks = [
            IndexedChunk(
                id=chunk.id,
                source_type="course",
                content=chunk.content,
                course_id=chunk.course_id,
                lesson_ids=chunk.lesson_ids or [],
                section_title=chunk.section_title,
            )
            for chunk in course_chunks
            if chunk.embedding
        ]
        vectors = [chunk.embedding for chunk in course_chunks if chunk.embedding]

        for chunk in knowledge_chunks:
            if not chunk.embedding:
                continue
            indexed_chunks.append(
                IndexedChunk(
                    id=chunk.id,
                    source_type="knowledge",
                    content=chunk.content,
                    course_id="",
                    lesson_ids=[],
                    document_id=chunk.document_id,
                    document_title=chunk.document.title if chunk.document else None,
                    source_url=chunk.document.source_url if chunk.document else None,
                )
            )
            vectors.append(chunk.embedding)

        with self._lock:
            if not indexed_chunks or not vectors:
                self._index = None
                self._chunks = []
                self._dimension = 0
                logger.info("RAG FAISS index cleared because no chunks are available")
                return

            matrix = np.array(vectors, dtype="float32")
            if matrix.ndim != 2:
                self._index = None
                self._chunks = []
                self._dimension = 0
                logger.warning("RAG FAISS index rebuild skipped due to invalid embedding shape")
                return

            faiss.normalize_L2(matrix)
            index = faiss.IndexFlatIP(matrix.shape[1])
            index.add(matrix)

            self._index = index
            self._chunks = indexed_chunks
            self._dimension = matrix.shape[1]
            logger.info("RAG FAISS index rebuilt chunk_count=%s dimension=%s", len(self._chunks), self._dimension)

    def _refresh_embeddings_if_needed(
        self,
        db: Session,
        course_chunks: list[CourseChunk],
        knowledge_chunks: list[KnowledgeChunk],
    ) -> None:
        chunks = [chunk for chunk in [*course_chunks, *knowledge_chunks] if chunk.content]
        if not chunks:
            return

        expected_dimension = len(embed_documents(["embedding dimension probe"])[0])
        if all(chunk.embedding and len(chunk.embedding) == expected_dimension for chunk in chunks):
            return

        logger.info(
            "Refreshing RAG embeddings for current model chunk_count=%s dimension=%s",
            len(chunks),
            expected_dimension,
        )
        embeddings = embed_documents([chunk.content for chunk in chunks])
        for chunk, embedding in zip(chunks, embeddings):
            chunk.embedding = embedding
        db.commit()

    def search(
        self,
        query_embedding: list[float],
        top_k: int,
        source_type: str | None = None,
    ) -> list[tuple[IndexedChunk, float]]:
        with self._lock:
            if self._index is None or not self._chunks:
                return []

            query = np.array([query_embedding], dtype="float32")
            if query.shape[1] != self._dimension:
                logger.warning(
                    "RAG query embedding dimension mismatch expected=%s actual=%s",
                    self._dimension,
                    query.shape[1],
                )
                return []

            faiss.normalize_L2(query)
            search_k = min(max(top_k, top_k * 5 if source_type else top_k), len(self._chunks))
            scores, indices = self._index.search(query, search_k)

            results = []
            for score, index in zip(scores[0], indices[0]):
                if index < 0:
                    continue
                chunk = self._chunks[index]
                if source_type and chunk.source_type != source_type:
                    continue
                results.append((chunk, float(score)))
                if len(results) >= top_k:
                    break
            return results


rag_index = RAGIndex()


def rebuild_rag_index(db: Session) -> None:
    rag_index.rebuild(db)
