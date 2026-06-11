import json
import logging
import re
from pathlib import Path

from app.core.config import settings
from app.models import ASRSegment, SentenceItem, to_jsonable


logger = logging.getLogger(__name__)
SENTENCE_END_RE = re.compile(r"(?<=[.!?。！？])\s+")
SOFT_BOUNDARY_RE = re.compile(r"(?<=[,;:，；：])\s+")


def split_text_by_rules(text: str) -> list[str]:
    text = re.sub(r"\s+", " ", text or "").strip()
    if not text:
        return []

    output: list[str] = []
    for part in SENTENCE_END_RE.split(text):
        part = part.strip()
        if not part:
            continue
        if len(part) <= settings.MAX_SENTENCE_CHARS_BEFORE_LLM:
            output.append(part)
        else:
            output.extend([item.strip() for item in SOFT_BOUNDARY_RE.split(part) if item.strip()])
    return output


def split_segments_to_sentences(segments: list[ASRSegment]) -> list[SentenceItem]:
    sentences: list[SentenceItem] = []
    next_id = 0
    for segment in segments:
        parts = split_text_by_rules(segment.text)
        if not parts:
            continue

        total_chars = sum(max(1, len(part)) for part in parts)
        cursor = segment.start
        duration = max(0.01, segment.end - segment.start)

        for part in parts:
            part_duration = duration * max(1, len(part)) / total_chars
            sentences.append(
                SentenceItem(
                    id=next_id,
                    source_segment_id=segment.id,
                    start=cursor,
                    end=cursor + part_duration,
                    text=part,
                )
            )
            next_id += 1
            cursor += part_duration
    return sentences


def refine_long_sentences_with_llm(
    items: list[SentenceItem],
    llm,
    artifact_dir: Path | None = None,
) -> list[SentenceItem]:
    refined: list[SentenceItem] = []
    next_id = 0
    for item in items:
        if len(item.text) <= settings.MAX_SENTENCE_CHARS_BEFORE_LLM:
            refined.append(SentenceItem(next_id, item.source_segment_id, item.start, item.end, item.text))
            next_id += 1
            continue

        write_json_artifact(artifact_dir, f"05_llm_split_long_sentence_{item.id}_input.json", item)
        chunks = llm.split_long_sentence(item.text) or [item.text]
        write_json_artifact(
            artifact_dir,
            f"05_llm_split_long_sentence_{item.id}_output.json",
            {"id": item.id, "chunks": chunks},
        )
        total_chars = sum(max(1, len(chunk)) for chunk in chunks)
        cursor = item.start
        duration = max(0.01, item.end - item.start)

        for chunk in chunks:
            chunk = chunk.strip()
            if not chunk:
                continue
            chunk_duration = duration * max(1, len(chunk)) / total_chars
            refined.append(
                SentenceItem(
                    id=next_id,
                    source_segment_id=item.source_segment_id,
                    start=cursor,
                    end=cursor + chunk_duration,
                    text=chunk,
                )
            )
            next_id += 1
            cursor += chunk_duration
    return refined


def write_json_artifact(artifact_dir: Path | None, file_name: str, value) -> None:
    if artifact_dir is None:
        return
    try:
        artifact_dir.mkdir(parents=True, exist_ok=True)
        (artifact_dir / file_name).write_text(
            json.dumps(to_jsonable(value), ensure_ascii=False, indent=2),
            encoding="utf-8",
        )
    except Exception:
        logger.warning("Failed to write segmentation artifact file=%s", file_name, exc_info=True)
