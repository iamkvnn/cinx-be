import uuid
from datetime import datetime

from sqlalchemy import delete
from sqlalchemy.orm import Session

from app.agent.schemas import Citation
from app.entities.knowledge import KnowledgeChunk, KnowledgeDocument
from app.services.rag_index import rag_index, rebuild_rag_index
from app.services.embedding_model import embed_documents, embed_query


class KnowledgeService:
    def __init__(self, db: Session):
        self.db = db

    def import_document(
        self,
        title: str,
        content: str,
        source_type: str = "CMS",
        source_url: str | None = None,
    ) -> KnowledgeDocument:
        document = KnowledgeDocument(
            id=str(uuid.uuid4()),
            title=title,
            source_type=source_type,
            source_url=source_url,
            content=content,
            created_at=datetime.utcnow(),
            updated_at=datetime.utcnow(),
        )
        self.db.add(document)
        self.db.flush()

        chunks = self._chunk_text(content)
        embeddings = embed_documents(chunks)
        for chunk_text, embedding in zip(chunks, embeddings):
            self.db.add(
                KnowledgeChunk(
                    id=str(uuid.uuid4()),
                    document_id=document.id,
                    content=chunk_text,
                    embedding=embedding,
                    chunk_metadata={},
                )
            )
        self.db.commit()
        rebuild_rag_index(self.db)
        return document

    def replace_document(
        self,
        document_id: str,
        title: str,
        content: str,
        source_type: str = "CMS",
        source_url: str | None = None,
    ) -> KnowledgeDocument:
        self.db.execute(delete(KnowledgeChunk).where(KnowledgeChunk.document_id == document_id))
        document = self.db.get(KnowledgeDocument, document_id)
        if document is None:
            document = KnowledgeDocument(id=document_id, title=title, source_type=source_type, source_url=source_url, content=content)
            self.db.add(document)
        else:
            document.title = title
            document.source_type = source_type
            document.source_url = source_url
            document.content = content
            document.updated_at = datetime.utcnow()
        self.db.flush()

        chunks = self._chunk_text(content)
        embeddings = embed_documents(chunks)
        for chunk_text, embedding in zip(chunks, embeddings):
            self.db.add(
                KnowledgeChunk(
                    id=str(uuid.uuid4()),
                    document_id=document.id,
                    content=chunk_text,
                    embedding=embedding,
                    chunk_metadata={},
                )
            )
        self.db.commit()
        rebuild_rag_index(self.db)
        return document

    def retrieve(self, query: str, top_k: int = 5) -> tuple[list[str], list[Citation]]:
        query_embedding = embed_query(query)
        results = rag_index.search(query_embedding, top_k, source_type="knowledge")
        if not results:
            rebuild_rag_index(self.db)
            results = rag_index.search(query_embedding, top_k, source_type="knowledge")

        contexts = []
        citations = []
        for chunk, score in results:
            contexts.append(chunk.content)
            citations.append(
                Citation(
                    sourceType="knowledge",
                    title=chunk.document_title,
                    documentId=chunk.document_id,
                    sourceUrl=chunk.source_url,
                    score=score,
                )
            )
        return contexts, citations

    def _chunk_text(self, content: str, chunk_size: int = 1200, overlap: int = 150) -> list[str]:
        normalized = " ".join(content.split())
        if not normalized:
            return [content]
        chunks = []
        start = 0
        while start < len(normalized):
            end = min(start + chunk_size, len(normalized))
            chunks.append(normalized[start:end])
            if end == len(normalized):
                break
            start = max(end - overlap, 0)
        return chunks
