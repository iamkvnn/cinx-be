from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.services.rag_service import RAGService
from pydantic import BaseModel

router = APIRouter(prefix="/api/v1/recommendations/learning-path", tags=["learning-path"])

class LearningPathRequest(BaseModel):
    goal: str
    top_k: int = 15

@router.post("/generate")
def generate_learning_path(req: LearningPathRequest, db: Session = Depends(get_db)):
    service = RAGService(db)
    result = service.generate_learning_path(user_goal=req.goal, top_k=req.top_k)
    return result