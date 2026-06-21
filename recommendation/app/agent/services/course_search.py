import json
from dataclasses import asdict

from sqlalchemy.orm import Session

from app.agent.schemas import Citation
from app.repositories.course_repository import CourseFilters, CourseRepository
from app.services.llm_client import DigitalOceanLLMClient
from app.services.rag_index import rag_index, rebuild_rag_index
from app.services.embedding_model import embed_query


class CourseSearchService:
    def __init__(self, db: Session, llm_client: DigitalOceanLLMClient | None = None):
        self.db = db
        self.course_repo = CourseRepository(db)
        self.llm = llm_client or DigitalOceanLLMClient()

    def search(self, query: str, top_k: int = 5) -> tuple[list[dict], list[Citation], CourseFilters]:
        top_k = max(1, min(int(top_k or 5), 5))
        filters = self.extract_filters(query)
        semantic_scores = self._semantic_course_scores(query, top_k=max(top_k * 4, 20))
        structured_courses = self.course_repo.search_published_courses(filters, limit=max(top_k * 4, 20))
        if not structured_courses and filters.category_name and filters.query:
            structured_courses = self.course_repo.search_published_courses(
                CourseFilters(
                    category_id=filters.category_id,
                    category_name=filters.category_name,
                    min_rating=filters.min_rating,
                    price_from=filters.price_from,
                    price_to=filters.price_to,
                    has_certificate=filters.has_certificate,
                    is_in_subscription=filters.is_in_subscription,
                    max_duration=filters.max_duration,
                ),
                limit=max(top_k * 4, 20),
            )
        structured_ids = {course.id for course in structured_courses}

        if semantic_scores:
            ordered_ids = list(semantic_scores.keys())
            if filters.has_structured_filters() and structured_ids:
                preferred = [course_id for course_id in ordered_ids if course_id in structured_ids]
                structured_remainder = [course.id for course in structured_courses if course.id not in preferred]
                remainder = [course_id for course_id in ordered_ids if course_id not in structured_ids]
                ordered_ids = preferred + structured_remainder + remainder
            courses = self.course_repo.get_by_ids(ordered_ids)
        else:
            courses = structured_courses

        if not courses:
            courses = self.course_repo.search_published_courses(CourseFilters(query=filters.query or query), limit=top_k)

        scored = []
        for course in courses:
            semantic = semantic_scores.get(course.id, 0.0)
            filter_boost = 0.25 if course.id in structured_ids else 0.0
            if filters.category_name and _normalize(course.category_name) == _normalize(filters.category_name):
                filter_boost += 0.25
            rating_boost = min(float(course.rating or 0) / 5.0, 1.0) * 0.12
            enrollment_boost = min(float(course.enrollment_count or 0) / 1000.0, 1.0) * 0.08
            scored.append((course, semantic + filter_boost + rating_boost + enrollment_boost))

        scored.sort(key=lambda item: item[1], reverse=True)
        pre_ranked_results = [self._course_to_result(course, score) for course, score in scored[:5]]
        results = self._rerank_filter_results(query, filters, pre_ranked_results)[:top_k]
        citations = [
            Citation(
                sourceType="course",
                title=result["title"],
                courseId=result["courseId"],
                sourceUrl=f"https://shiny.id.vn/courses/{result['courseId']}",
                score=result["score"],
            )
            for result in results
        ]
        return results, citations, filters

    def _rerank_filter_results(self, query: str, filters: CourseFilters, results: list[dict]) -> list[dict]:
        if len(results) <= 1:
            return results

        candidate_by_id = {result["courseId"]: result for result in results if result.get("courseId")}
        prompt_candidates = [
            {
                "courseId": result["courseId"],
                "title": result.get("title"),
                "description": _short_text(result.get("description"), 500),
                "categoryName": result.get("categoryName"),
                "instructorName": result.get("instructorName"),
                "rating": result.get("rating"),
                "price": result.get("price"),
                "discountedPrice": result.get("discountedPrice"),
                "duration": result.get("duration"),
                "hasCertificate": result.get("hasCertificate"),
                "score": result.get("score"),
            }
            for result in results[:5]
        ]
        prompt = f"""
Filter and rerank these course search candidates for relevance to the user request.

Return strict JSON only:
{{
  "courseIds": ["course id to keep, in best order"]
}}

Rules:
- Keep only courses directly relevant to the user request and filters.
- Use only courseIds from candidates.
- Return at most 5 courseIds.
- Return an empty list when none are relevant.

User request:
{query}

Extracted filters:
{json.dumps(filters_to_dict(filters), ensure_ascii=False)}

Candidates:
{json.dumps(prompt_candidates, ensure_ascii=False)}
"""
        try:
            parsed = self.llm.generate_json_required(
                prompt,
                system="You rerank course search candidates. Return strict JSON only.",
                temperature=0.0,
                label="course_search_rerank",
            )
        except Exception:
            return results[:5]

        selected_ids = parsed.get("courseIds") or parsed.get("course_ids") or []
        reranked = []
        seen = set()
        for course_id in selected_ids:
            course_id = str(course_id)
            if course_id in candidate_by_id and course_id not in seen:
                reranked.append(candidate_by_id[course_id])
                seen.add(course_id)
            if len(reranked) >= 5:
                break
        return reranked

    def extract_filters(self, query: str) -> CourseFilters:
        """Extract structured filters from the user query using LLM only.

        The previous heuristic parser (_heuristic_filters) has been removed.
        The LLM must determine all filter fields; if it fails the LLMRetryError
        propagates to the caller — no silent fallback.
        """
        categories = self.course_repo.list_published_categories()

        prompt = f"""
Extract course search filters from this user query. Return JSON only.

Allowed keys:
query, category_name, category_id, min_rating, price_from, price_to, has_certificate, is_in_subscription, max_duration

Use null when unknown.
Choose category_name from the existing categories when the user's language or wording clearly maps to one.
Only set category_id when it is copied from the matching existing category.
If category_name captures the whole search request, set query to null instead of repeating the category phrase.
Do not invent categories.
max_duration is in minutes.

Existing categories:
{json.dumps(categories, ensure_ascii=False)}

User query: {query}
"""
        parsed = self.llm.generate_json_required(
            prompt,
            system="You extract strict JSON filters for course search. Return JSON only.",
            temperature=0.0,
            label="course_filter_extraction",
        )
        category_id, category_name = _resolve_category(
            _string_or_none(parsed.get("category_id")),
            _string_or_none(parsed.get("category_name") or parsed.get("categoryName")),
            categories,
        )
        parsed_query = _string_or_none(parsed.get("query"))

        return CourseFilters(
            query=parsed_query,
            category_id=category_id,
            category_name=category_name,
            min_rating=_number_or_none(parsed.get("min_rating")),
            price_from=_number_or_none(parsed.get("price_from")),
            price_to=_number_or_none(parsed.get("price_to")),
            has_certificate=_bool_or_none(parsed.get("has_certificate")),
            is_in_subscription=_bool_or_none(parsed.get("is_in_subscription")),
            max_duration=_int_or_none(parsed.get("max_duration")),
        )

    def _semantic_course_scores(self, query: str, top_k: int) -> dict[str, float]:
        query_embedding = embed_query(query)
        results = rag_index.search(query_embedding, top_k, source_type="course")
        if not results:
            rebuild_rag_index(self.db)
            results = rag_index.search(query_embedding, top_k, source_type="course")

        scores: dict[str, float] = {}
        for chunk, score in results:
            if not chunk.course_id:
                continue
            scores[chunk.course_id] = max(scores.get(chunk.course_id, float("-inf")), score)
        return dict(sorted(scores.items(), key=lambda item: item[1], reverse=True))

    def _course_to_result(self, course, score: float) -> dict:
        return {
            "courseId": course.id,
            "title": course.title,
            "description": course.description,
            "categoryId": course.category_id,
            "categoryName": course.category_name,
            "instructorId": course.instructor_id,
            "instructorName": course.instructor_name,
            "rating": course.rating,
            "enrollmentCount": course.enrollment_count,
            "price": course.price,
            "discountedPrice": course.discounted_price,
            "duration": course.duration,
            "hasCertificate": course.has_certificate,
            "certificateTitle": course.certificate_title,
            "isInSubscription": course.is_in_subscription,
            "imageUrls": course.image_urls or [],
            "score": round(score, 4),
            "url": f"https://shiny.id.vn/courses/{course.id}",
        }


def filters_to_dict(filters: CourseFilters) -> dict:
    return asdict(filters)


def _number_or_none(value) -> float | None:
    if value is None:
        return None
    try:
        return float(value)
    except (TypeError, ValueError):
        return None


def _int_or_none(value) -> int | None:
    number = _number_or_none(value)
    return int(number) if number is not None else None


def _bool_or_none(value, default=None) -> bool | None:
    if isinstance(value, bool):
        return value
    if isinstance(value, str):
        if value.lower() in ("true", "yes", "1"):
            return True
        if value.lower() in ("false", "no", "0"):
            return False
    return default


def _string_or_none(value) -> str | None:
    if value is None:
        return None
    text = str(value).strip()
    return text or None


def _resolve_category(
    category_id: str | None,
    category_name: str | None,
    categories: list[dict[str, str | None]],
) -> tuple[str | None, str | None]:
    by_id = {category["category_id"]: category for category in categories if category.get("category_id")}
    if category_id and category_id in by_id:
        category = by_id[category_id]
        return category.get("category_id"), category.get("category_name")

    if not category_name:
        return None, None

    normalized_name = _normalize(category_name)
    for category in categories:
        if _normalize(category.get("category_name")) == normalized_name:
            return category.get("category_id"), category.get("category_name")
    for category in categories:
        existing = _normalize(category.get("category_name"))
        if normalized_name in existing or existing in normalized_name:
            return category.get("category_id"), category.get("category_name")
    return None, None


def _normalize(value) -> str:
    return " ".join(str(value or "").lower().split())


def _short_text(value: str | None, limit: int) -> str | None:
    if value is None:
        return None
    text = str(value)
    if len(text) <= limit:
        return text
    return text[: limit - 3].rstrip() + "..."
