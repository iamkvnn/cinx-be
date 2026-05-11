from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime

class LessonPayload(BaseModel):
    id: str
    title: str
    lessonType: Optional[str] = None  # 'VIDEO', 'ARTICLE', 'QUIZ', 'ASSIGNMENT'
    orderIndex: int
    duration: Optional[int] = 0


class SectionPayload(BaseModel):
    id: str
    title: str
    description: Optional[str] = None
    orderIndex: int
    duration: Optional[int] = None
    lessons: List[LessonPayload] = []


class CategoryPayload(BaseModel):
    id: str
    name: str


class InstructorPayload(BaseModel):
    id: str
    name: str
    email: Optional[str] = None
    gender: Optional[str] = None  # 'MALE' | 'FEMALE' | ...
    avatarUrl: Optional[str] = None


class ImagePayload(BaseModel):
    id: str
    imageUrl: str


class CoursePayload(BaseModel):
    id: str
    title: str
    description: Optional[str] = None
    category: CategoryPayload
    instructor: Optional[InstructorPayload] = None
    images: List[ImagePayload] = []
    price: Optional[float] = None
    discountedPrice: Optional[float] = None
    discountRate: Optional[int] = None
    rating: Optional[float] = 0.0
    enrollmentCount: int = 0
    isInSubscription: bool = False
    duration: Optional[int] = None
    hasCertificate: bool = False
    certificateTitle: Optional[str] = None
    status: str = "DRAFT"  # 'DRAFT' | 'PUBLISHED' | ...
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
    categorieIds: list[str]

class UserPreferenceEvent(BaseModel):
    payload: UserPreferencePayload

class CourseInteractionPayload(BaseModel):
    userId: str
    courseId: str
    interactionType: str
    ratingValue: Optional[float] = None


class CourseInteractionEvent(BaseModel):
    payload: CourseInteractionPayload