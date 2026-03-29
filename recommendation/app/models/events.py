from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class CoursePayload(BaseModel):
    id: str
    title: str
    description: Optional[str] = None
    category: str
    price: Optional[float] = None
    discountedPrice: Optional[float] = None
    discountRate: Optional[int] = None
    rating: Optional[float] = 0.0
    enrollmentCount: int = 0
    isPublished: bool = False
    isInSubscription: bool = False
    duration: Optional[int] = None
    createdAt: Optional[datetime] = None
    updatedAt: Optional[datetime] = None


class CourseEvent(BaseModel):
    course: CoursePayload
    timestamp: Optional[datetime] = None


class EnrolledCourseEvent(BaseModel):
    userId: str
    courseId: str


class WishlistEvent(BaseModel):
    userId: str
    courseId: str
    added: bool


class UserPreferencePayload(BaseModel):
    userId: str
    categories: list[str]

class UserPreferenceEvent(BaseModel):
    payload: UserPreferencePayload

class CourseInteractionPayload(BaseModel):
    userId: str
    courseId: str
    interactionType: str
    ratingValue: Optional[float] = None


class CourseInteractionEvent(BaseModel):
    payload: CourseInteractionPayload