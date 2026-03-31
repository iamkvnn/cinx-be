from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime


class LessonPayload(BaseModel):
    id: str
    title: str
    lessonType: Optional[str] = None # 'video', 'article', 'quiz', 'assignment'
    orderIndex: int
    duration: Optional[int] = 0
    description: Optional[str] = None

class SectionPayload(BaseModel):
    id: str
    title: str
    description: Optional[str] = None
    orderIndex: int
    lessons: List[LessonPayload] = []


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
    sections: List[SectionPayload] = []
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