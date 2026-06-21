from datetime import datetime
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from app.repositories.course_repository import CourseRepository
from app.repositories.user_repository import UserRepository
from app.repositories.interaction_repository import InteractionRepository


class RecommendationService:
    def __init__(self, db):
        self.course_repo = CourseRepository(db)
        self.user_repo = UserRepository(db)
        self.interaction_repo = InteractionRepository(db)

    def recommend_for_user(self, user_id: str, top_k: int = 10):
        interaction_count = self.interaction_repo.count_user_interactions(user_id)

        if interaction_count < 1:
            result = self._onboarding_recommendation(user_id, top_k)
            if result:
                return result

        result = self._content_based_recommendation(user_id, top_k)
        if result:
            return result

        return self._fallback_popular(top_k)

    def _onboarding_recommendation(self, user_id: str, top_k: int):
        categories = self.user_repo.get_user_categories(user_id)
        if not categories:
            return []

        courses = self.course_repo.get_published_courses_by_categoryId(categories)

        scored = []
        for c in courses:
            score = 0.0
            if c.category_id in categories:
                score += 50
            score += min(c.rating / 5.0, 1.0) * 30
            score += min(c.enrollment_count / 1000.0, 1.0) * 20
            scored.append((c, score))

        scored.sort(key=lambda x: x[1], reverse=True)
        return [self._to_dict(c, score, "ONBOARDING") for c, score in scored[:top_k]]

    def _content_based_recommendation(self, user_id: str, top_k: int):
        interacted_ids = self.interaction_repo.get_user_interacted_course_ids(user_id)
        if not interacted_ids:
            return []

        all_courses = self.course_repo.get_published_courses()
        if len(all_courses) < 2:
            return []

        course_map = {c.id: c for c in all_courses}
        interacted_courses = [course_map[cid] for cid in interacted_ids if cid in course_map]
        candidate_courses = [c for c in all_courses if c.id not in interacted_ids]

        if not interacted_courses or not candidate_courses:
            return []

        docs = [
            f"{c.title} {c.category_name}"
            for c in all_courses
        ]

        vectorizer = TfidfVectorizer(stop_words=None)
        tfidf_matrix = vectorizer.fit_transform(docs).toarray()

        id_to_index = {c.id: idx for idx, c in enumerate(all_courses)}

        interacted_indices = [id_to_index[c.id] for c in interacted_courses]
        candidate_indices = [id_to_index[c.id] for c in candidate_courses]

        user_profile = tfidf_matrix[interacted_indices].mean(axis=0).reshape(1, -1)
        sims = cosine_similarity(user_profile, tfidf_matrix[candidate_indices]).flatten()

        scored = []
        for i, sim in enumerate(sims):
            c = candidate_courses[i]
            score = sim * 80 + min(c.rating / 5.0, 1.0) * 10 + min(c.enrollment_count / 1000.0, 1.0) * 10
            scored.append((c, float(score)))

        scored.sort(key=lambda x: x[1], reverse=True)
        return [self._to_dict(c, score, "CONTENT_BASED") for c, score in scored[:top_k]]

    def _fallback_popular(self, top_k: int):
        courses = self.course_repo.get_published_courses()

        def score(c):
            freshness = 0
            if c.created_at:
                age_days = max((datetime.now() - c.created_at).days, 0)
                freshness = max(0, 30 - min(age_days, 30)) / 30 * 10

            return min(c.rating / 5.0, 1.0) * 60 + min(c.enrollment_count / 1000.0, 1.0) * 30 + freshness

        ranked = sorted(courses, key=score, reverse=True)
        return [self._to_dict(c, score(c), "POPULAR_FALLBACK") for c in ranked[:top_k]]

    def _to_dict(self, c, score, source):
        return {
            "courseId": c.id
        }