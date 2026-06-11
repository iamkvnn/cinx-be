import json
import logging
import re
from pathlib import Path

from app.core.config import settings
from app.models import SentenceItem, SubtitleItem, to_jsonable


VTT_TIMESTAMP = re.compile(
    r"(?P<start>\d{2}:\d{2}:\d{2}\.\d{3})\s+-->\s+(?P<end>\d{2}:\d{2}:\d{2}\.\d{3})"
)


def parse_webvtt(content: str) -> list[SubtitleItem]:
    normalized = strip_bom(content).replace("\r\n", "\n").replace("\r", "\n")
    blocks = re.split(r"\n\s*\n", normalized.strip())
    items: list[SubtitleItem] = []
    for block in blocks:
        lines = [line.strip() for line in block.splitlines() if line.strip()]
        if not lines or lines[0].upper().startswith("WEBVTT"):
            continue
        timestamp_index = next((idx for idx, line in enumerate(lines) if "-->" in line), None)
        if timestamp_index is None:
            continue
        match = VTT_TIMESTAMP.search(lines[timestamp_index])
        if not match:
            continue
        text = "\n".join(lines[timestamp_index + 1 :]).strip()
        if not text:
            continue
        items.append(
            SubtitleItem(
                index=len(items) + 1,
                start=parse_vtt_timestamp(match.group("start")),
                end=parse_vtt_timestamp(match.group("end")),
                text=text,
            )
        )
    return items


def build_final_subtitles(
    items: list[SentenceItem],
    llm,
    target_language: str,
    artifact_dir: Path | None = None,
) -> list[SubtitleItem]:
    output: list[SubtitleItem] = []
    candidates = [item for item in items if item.text.strip()]
    split_map = split_translated_items_for_subtitles(candidates, llm, target_language, artifact_dir)
    write_json_artifact(artifact_dir, "08_final_subtitle_split_map.json", split_map)

    for item in candidates:
        chunks = split_map.get(item.id) or smart_rule_subtitle_split(item.text)
        for cue in distribute_time(item.start, item.end, chunks):
            if cue["end"] - cue["start"] < 0.25:
                cue["end"] = cue["start"] + 0.25
            output.append(
                SubtitleItem(
                    index=len(output) + 1,
                    start=cue["start"],
                    end=cue["end"],
                    text=cue["text"],
                )
            )
    return output


def sentence_items_from_cues(cues: list[SubtitleItem]) -> list[SentenceItem]:
    return [
        SentenceItem(
            id=index,
            source_segment_id=index,
            start=cue.start,
            end=cue.end,
            text=" ".join(cue.text.split()),
        )
        for index, cue in enumerate(cues)
    ]


def write_webvtt(subtitles: list[SubtitleItem]) -> str:
    lines = ["WEBVTT", ""]
    for sub in subtitles:
        lines.append(f"{format_vtt_timestamp(sub.start)} --> {format_vtt_timestamp(sub.end)}")
        lines.extend(sub.text.splitlines())
        lines.append("")
    return "\n".join(lines).rstrip() + "\n"


def write_srt(subtitles: list[SubtitleItem]) -> str:
    lines: list[str] = []
    for sub in subtitles:
        lines.append(str(sub.index))
        lines.append(f"{format_srt_timestamp(sub.start)} --> {format_srt_timestamp(sub.end)}")
        lines.extend(sub.text.splitlines())
        lines.append("")
    return "\n".join(lines).rstrip() + "\n"


def validate_subtitles(subtitles: list[SubtitleItem]) -> None:
    if not subtitles:
        raise ValueError("No subtitle cues generated")
    previous_start = -1.0
    for index, sub in enumerate(subtitles, start=1):
        if sub.index != index:
            raise ValueError("Subtitle indexes must be sequential")
        if sub.start < previous_start:
            raise ValueError("Subtitle start time moved backwards")
        if sub.end <= sub.start:
            raise ValueError("Subtitle end must be greater than start")
        lines = sub.text.splitlines()
        if not lines or len(lines) > settings.MAX_SUBTITLE_LINES:
            raise ValueError("Subtitle text violates line count constraints")
        for line in lines:
            if len(line) > settings.MAX_SUBTITLE_LINE_CHARS + 12:
                raise ValueError("Subtitle line is too long")
        previous_start = sub.start


def distribute_time(start: float, end: float, chunks: list[str]) -> list[dict]:
    cleaned = [chunk for chunk in chunks if chunk and chunk.strip()]
    if not cleaned:
        return []
    if len(cleaned) == 1:
        return [{"start": start, "end": end, "text": cleaned[0]}]

    duration = max(0.01, end - start)
    weights = [max(1, len(chunk.replace("\n", " "))) for chunk in cleaned]
    total = sum(weights)
    cursor = start
    result: list[dict] = []
    for index, chunk in enumerate(cleaned):
        chunk_end = end if index == len(cleaned) - 1 else cursor + duration * weights[index] / total
        result.append({"start": cursor, "end": chunk_end, "text": chunk})
        cursor = chunk_end
    return result


def normalize_text(text: str) -> str:
    return re.sub(r"\s+", " ", text or "").strip()


def wrap_one_or_two_lines(text: str) -> list[str]:
    text = normalize_text(text)
    if not text:
        return []
    words = text.split()
    lines: list[str] = []
    current = ""
    for word in words:
        candidate = f"{current} {word}".strip()
        if len(candidate) <= settings.MAX_SUBTITLE_LINE_CHARS:
            current = candidate
        else:
            if current:
                lines.append(current)
            current = word
    if current:
        lines.append(current)
    return [
        "\n".join(lines[index : index + settings.MAX_SUBTITLE_LINES])
        for index in range(0, len(lines), settings.MAX_SUBTITLE_LINES)
    ]


def smart_rule_subtitle_split(text: str) -> list[str]:
    normalized = normalize_text(text)
    if not normalized:
        return []
    if len(normalized) <= settings.MAX_SUBTITLE_LINE_CHARS * settings.MAX_SUBTITLE_LINES:
        return wrap_one_or_two_lines(normalized)

    chunks: list[str] = []
    parts = re.split(r"(?<=[.!?。！？,;:，；：])\s+", normalized)
    parts = [part.strip() for part in parts if part.strip()] or [normalized]
    for part in parts:
        chunks.extend(wrap_one_or_two_lines(part))
    return [chunk for chunk in chunks if chunk.strip()]


def sanitize_llm_chunks(original: str, chunks: list[str]) -> list[str]:
    if not chunks:
        return smart_rule_subtitle_split(original)

    result: list[str] = []
    for chunk in chunks:
        if not isinstance(chunk, str):
            continue
        lines = [normalize_text(line) for line in chunk.split("\n")]
        chunk = "\n".join([line for line in lines if line])
        if is_valid_chunk(chunk):
            result.append(chunk)
        else:
            result.extend(smart_rule_subtitle_split(chunk))
    return result or smart_rule_subtitle_split(original)


def is_valid_chunk(chunk: str) -> bool:
    if not chunk or not chunk.strip():
        return False
    lines = chunk.splitlines()
    if len(lines) > settings.MAX_SUBTITLE_LINES:
        return False
    return all(len(line.strip()) <= settings.MAX_SUBTITLE_LINE_CHARS + 12 for line in lines)


def needs_final_split(item: SentenceItem) -> bool:
    text = normalize_text(item.text)
    duration = max(0.01, item.end - item.start)
    if len(text) > settings.MAX_SUBTITLE_LINE_CHARS * settings.MAX_SUBTITLE_LINES:
        return True
    if len(text) / duration > settings.MAX_SUBTITLE_CPS and len(text) > settings.MAX_SUBTITLE_LINE_CHARS:
        return True
    return len(wrap_one_or_two_lines(text)) > 1


def split_translated_items_for_subtitles(
    items: list[SentenceItem],
    llm,
    target_language: str,
    artifact_dir: Path | None = None,
) -> dict[int, list[str]]:
    result: dict[int, list[str]] = {}
    need_llm: list[SentenceItem] = []
    for item in items:
        if settings.USE_LLM_FOR_FINAL_SUBTITLE_SPLIT and needs_final_split(item):
            need_llm.append(item)
        else:
            result[item.id] = wrap_one_or_two_lines(item.text) or [item.text]

    for batch_index, batch in enumerate(chunk_list(need_llm, settings.FINAL_SPLIT_BATCH_SIZE), start=1):
        llm_input = [
            {"id": item.id, "text": item.text, "duration_seconds": item.end - item.start}
            for item in batch
        ]
        write_json_artifact(artifact_dir, f"07_llm_final_split_batch_{batch_index}_input.json", llm_input)
        try:
            llm_output = llm.split_subtitle_batch(llm_input, target_language)
            write_json_artifact(artifact_dir, f"07_llm_final_split_batch_{batch_index}_output.json", llm_output)
            output_map = normalize_split_output(llm_output)
            for item in batch:
                result[item.id] = sanitize_llm_chunks(item.text, output_map.get(item.id, []))
        except Exception as exc:
            logging.getLogger(__name__).warning("LLM final split failed, using rule-based split: %s", exc)
            write_json_artifact(
                artifact_dir,
                f"07_llm_final_split_batch_{batch_index}_fallback.json",
                {"error": repr(exc), "input": llm_input},
            )
            for item in batch:
                result[item.id] = smart_rule_subtitle_split(item.text)
    return result


def normalize_split_output(value) -> dict[int, list[str]]:
    if isinstance(value, dict):
        return {int(key): chunks for key, chunks in value.items() if str(key).isdigit() and isinstance(chunks, list)}
    result: dict[int, list[str]] = {}
    if not isinstance(value, list):
        return result
    for item in value:
        if not isinstance(item, dict) or "id" not in item:
            continue
        chunks = item.get("chunks", [])
        if isinstance(chunks, list):
            result[int(item["id"])] = chunks
    return result


def format_srt_timestamp(seconds: float) -> str:
    return format_vtt_timestamp(seconds).replace(".", ",")


def format_vtt_timestamp(seconds: float) -> str:
    seconds = max(0.0, float(seconds))
    millis = int(round((seconds - int(seconds)) * 1000))
    total_seconds = int(seconds)
    if millis >= 1000:
        total_seconds += 1
        millis -= 1000
    hours = total_seconds // 3600
    minutes = (total_seconds % 3600) // 60
    secs = total_seconds % 60
    return f"{hours:02}:{minutes:02}:{secs:02}.{millis:03}"


def parse_vtt_timestamp(value: str) -> float:
    hours, minutes, rest = value.split(":")
    seconds, millis = rest.split(".")
    return int(hours) * 3600 + int(minutes) * 60 + int(seconds) + int(millis) / 1000


def strip_bom(content: str) -> str:
    return content[1:] if content.startswith("\ufeff") else content


def chunk_list(items: list, size: int) -> list[list]:
    return [items[index : index + size] for index in range(0, len(items), size)]


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
        logging.getLogger(__name__).warning("Failed to write subtitle artifact file=%s", file_name, exc_info=True)
