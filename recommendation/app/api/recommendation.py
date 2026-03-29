from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from app.core.database import get_db
from app.services.hybrid import RecommendationService

router = APIRouter(prefix="/api/v1/recommendations", tags=["recommendations"])


@router.get("/users/{user_id}")
def recommend_for_user(user_id: str, top_k: int = Query(10, ge=1, le=50), db: Session = Depends(get_db)):
    service = RecommendationService(db)
    return {
        "userId": user_id,
        "recommendations": service.recommend_for_user(user_id, top_k)
    }