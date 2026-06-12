import logging
import threading
from dataclasses import dataclass

import faiss
import numpy as np
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.entities.course_chunk import CourseChunk


logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class IndexedChunk:
    course_id: str
    lesson_ids: list[str]


class RAGIndex:
    def __init__(self):
        self._lock = threading.RLock()
        self._index = None
        self._chunks: list[IndexedChunk] = []
        self._dimension = 0

    def rebuild(self, db: Session) -> None:
        chunks = db.execute(select(CourseChunk)).scalars().all()
        indexed_chunks = [
            IndexedChunk(course_id=chunk.course_id, lesson_ids=chunk.lesson_ids or [])
            for chunk in chunks
            if chunk.embedding
        ]
        vectors = [chunk.embedding for chunk in chunks if chunk.embedding]

        with self._lock:
            if not chunks or not vectors:
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

    def search(self, query_embedding: list[float], top_k: int) -> list[tuple[IndexedChunk, float]]:
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
            scores, indices = self._index.search(query, min(top_k, len(self._chunks)))

            results = []
            for score, index in zip(scores[0], indices[0]):
                if index < 0:
                    continue
                results.append((self._chunks[index], float(score)))
            return results


rag_index = RAGIndex()


def rebuild_rag_index(db: Session) -> None:
    rag_index.rebuild(db)
