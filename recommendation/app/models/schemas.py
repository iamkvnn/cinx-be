from typing import List, Optional
from pydantic import BaseModel


class OnboardingRecommendationRequest(BaseModel):
    user_id: Optional[int] = None
    category_ids: List[int]
    level: Optional[str] = None
    language: Optional[str] = None
    top_k: int = 10


class RecommendationItem(BaseModel):
    course_id: int
    score: float


class RecommendationResponse(BaseModel):
    mode: str
    recommendations: List[RecommendationItem]