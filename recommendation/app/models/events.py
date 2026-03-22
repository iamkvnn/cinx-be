from pydantic import BaseModel
from datetime import datetime
from typing import Optional, List


class BaseEvent(BaseModel):
    eventType: str
    occurredAt: datetime


class CoursePayload(BaseModel):
    id: str
    title: str
    description: Optional[str] = None
    category: str
    price: Optional[float] = None
    discountedPrice: Optional[float] = None
    discountRate: Optional[int] = None
    rating: float = 0.0
    enrollmentCount: int = 0
    isPublished: bool = False
    isInSubscription: bool = False
    duration: Optional[int] = None
    createdAt: Optional[datetime] = None
    updatedAt: Optional[datetime] = None


class CourseEvent(BaseEvent):
    payload: CoursePayload


class UserPreferencePayload(BaseModel):
    userId: str
    categories: List[str]


class UserPreferenceEvent(BaseEvent):
    payload: UserPreferencePayload


class CourseInteractionPayload(BaseModel):
    userId: str
    courseId: str
    interactionType: str
    ratingValue: Optional[float] = None


class CourseInteractionEvent(BaseEvent):
    payload: CourseInteractionPayload