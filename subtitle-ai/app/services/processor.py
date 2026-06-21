import asyncio
import json
import logging
from pathlib import Path

from app.core.config import settings
from app.models import SentenceItem, to_jsonable
from app.services.asr import extract_audio, transcribe_with_faster_whisper, word_confidence_items
from app.services.alignment import align_sentence_items_to_words
from app.services.llm import DigitalOceanLLM
from app.services.segmentation import refine_long_sentences_with_llm, split_segments_to_sentences
from app.services.storage import S3Storage
from app.services.subtitle import (
    build_final_subtitles,
    chunk_list,
    parse_webvtt,
    sentence_items_from_cues,
    validate_subtitles,
    write_webvtt,
)


logger = logging.getLogger(__name__)
CONTENT_TYPE_VTT = "text/vtt"
CONTENT_TYPE_JSON = "application/json"


class SubtitleJobProcessor:
    def __init__(
        self,
        storage: S3Storage | None = None,
        llm: DigitalOceanLLM | None = None,
    ):
        self.storage = storage or S3Storage()
        self.llm = llm or DigitalOceanLLM()

    async def process_generate(self, event: dict, publisher) -> None:
        job_id = require(event, "jobId")
        lesson_id = require(event, "lessonId")
        video_file_key = require(event, "videoFileKey")
        expected_output_file_key = require(event, "expectedOutputFileKey")
        target_language_code = event.get("targetLanguageCode") or "auto"

        logger.info(
            "Generate subtitle job started job_id=%s lesson_id=%s video_file_key=%s target_language=%s output_key=%s",
            job_id,
            lesson_id,
            video_file_key,
            target_language_code,
            expected_output_file_key,
        )
        await publisher.publish_progress(job_id, 5)
        work_dir = Path(settings.WORK_DIR) / job_id
        work_dir.mkdir(parents=True, exist_ok=True)
        try:
            suffix = Path(video_file_key).suffix or ".media"
            write_json_artifact(work_dir, "00_generate_event.json", event)
            logger.info(
                "Downloading source media job_id=%s file_key=%s",
                job_id,
                video_file_key,
            )
            media_path = work_dir / f"01_downloaded_media{suffix}"
            await asyncio.to_thread(self.storage.download_to_file, video_file_key, media_path)
            logger.info(
                "Source media downloaded job_id=%s local_path=%s",
                job_id,
                media_path,
            )
            await publisher.publish_progress(job_id, 15)

            logger.info("Extracting audio job_id=%s media_path=%s", job_id, media_path)
            audio_path = await asyncio.to_thread(extract_audio, media_path, work_dir)
            await publisher.publish_progress(job_id, 25)

            logger.info("Running ASR job_id=%s audio_path=%s", job_id, audio_path)
            asr_result = await asyncio.to_thread(transcribe_with_faster_whisper, audio_path)
            write_json_artifact(work_dir, "02_asr_result.json", asr_result)
            source_language = normalize_language(asr_result["language"])
            logger.info(
                "ASR result job_id=%s source_language=%s language_probability=%s segments=%s",
                job_id,
                source_language,
                asr_result["language_probability"],
                len(asr_result["segments"]),
            )
            await publisher.publish_progress(job_id, 55)

            logger.info("Splitting ASR segments job_id=%s", job_id)
            sentences = split_segments_to_sentences(asr_result["segments"])
            write_json_artifact(work_dir, "03_local_split_sentences.json", sentences)
            logger.info(
                "Refining sentence splits job_id=%s sentences=%s",
                job_id,
                len(sentences),
            )
            refined = refine_long_sentences_with_llm(sentences, self.llm, work_dir)
            refined = align_sentence_items_to_words(refined, asr_result["segments"])
            write_json_artifact(work_dir, "06_refined_sentences.json", refined)
            logger.info(
                "Sentence refinement completed job_id=%s refined_items=%s",
                job_id,
                len(refined),
            )
            await publisher.publish_progress(job_id, 70)

            output_language = (
                source_language
                if target_language_code == "auto"
                else normalize_language(target_language_code)
            )
            logger.info(
                "Building final subtitles job_id=%s output_language=%s",
                job_id,
                output_language,
            )
            subtitles = build_final_subtitles(refined, self.llm, output_language, work_dir, asr_result["segments"])
            write_json_artifact(work_dir, "09_final_subtitles.json", subtitles)
            validate_subtitles(subtitles)
            vtt_content = write_webvtt(subtitles)
            write_text_artifact(work_dir, "10_final_subtitles.vtt", vtt_content)
            logger.info(
                "Uploading generated VTT job_id=%s cues=%s output_key=%s",
                job_id,
                len(subtitles),
                expected_output_file_key,
            )
            uploaded = await asyncio.to_thread(
                self.storage.upload_text,
                expected_output_file_key,
                vtt_content,
                CONTENT_TYPE_VTT,
            )
            await publisher.publish_progress(job_id, 90)

            confidence_key = word_confidence_key(expected_output_file_key)
            confidence_content = json.dumps(
                word_confidence_items(asr_result["segments"]),
                ensure_ascii=False,
            )
            write_text_artifact(work_dir, "11_word_confidence.json", confidence_content)
            logger.info(
                "Uploading word confidence job_id=%s output_key=%s",
                job_id,
                confidence_key,
            )
            confidence = await asyncio.to_thread(
                self.storage.upload_text,
                confidence_key,
                confidence_content,
                CONTENT_TYPE_JSON,
            )

            await publisher.publish_completed(
                {
                    "jobId": job_id,
                    "outputFileKey": uploaded["fileKey"],
                    "outputFileUrl": uploaded["fileUrl"],
                    "fileName": f"{job_id}.vtt",
                    "fileType": CONTENT_TYPE_VTT,
                    "fileSize": uploaded["fileSize"],
                    "languageCode": output_language,
                    "displayName": event.get("displayName") or output_language,
                    "wordConfidenceFileKey": confidence["fileKey"],
                    "wordConfidenceFileUrl": confidence["fileUrl"],
                }
            )
            logger.info(
                "Generate subtitle job completed job_id=%s output_key=%s confidence_key=%s",
                job_id,
                uploaded["fileKey"],
                confidence["fileKey"],
            )
        finally:
            logger.info(
                "Keeping subtitle job artifacts job_id=%s work_dir=%s",
                job_id,
                work_dir,
            )

    async def process_translate(self, event: dict, publisher) -> None:
        job_id = require(event, "jobId")
        source_file_key = require(event, "sourceFileKey")
        expected_output_file_key = require(event, "expectedOutputFileKey")
        source_language = normalize_language(event.get("sourceLanguageCode") or "und")
        target_language = normalize_language(require(event, "targetLanguageCode"))

        logger.info(
            "Translate subtitle job started job_id=%s source_file_key=%s "
            "source_language=%s target_language=%s output_key=%s",
            job_id,
            source_file_key,
            source_language,
            target_language,
            expected_output_file_key,
        )
        work_dir = Path(settings.WORK_DIR) / job_id
        work_dir.mkdir(parents=True, exist_ok=True)
        write_json_artifact(work_dir, "00_translate_event.json", event)
        await publisher.publish_progress(job_id, 10)
        logger.info(
            "Reading source subtitle job_id=%s file_key=%s",
            job_id,
            source_file_key,
        )
        source_content = await asyncio.to_thread(self.storage.read_text, source_file_key)
        write_text_artifact(work_dir, "01_source_subtitle.vtt", source_content)
        cues = parse_webvtt(source_content)
        if not cues:
            raise ValueError("Source subtitle has no readable cues")
        write_json_artifact(work_dir, "02_parsed_source_cues.json", cues)
        logger.info("Parsed source subtitle job_id=%s cues=%s", job_id, len(cues))
        await publisher.publish_progress(job_id, 25)

        source_text = "\n".join(cue.text for cue in cues)
        write_text_artifact(work_dir, "03_source_transcript.txt", source_text)
        logger.info("Summarizing and extracting terminology job_id=%s", job_id)
        analysis = self.llm.summarize_and_extract_terms(
            source_text,
            source_language,
            target_language,
        )
        write_json_artifact(work_dir, "04_llm_analysis_output.json", analysis)
        await publisher.publish_progress(job_id, 40)

        source_items = sentence_items_from_cues(cues)
        write_json_artifact(work_dir, "05_source_sentence_items.json", source_items)
        logger.info(
            "Translating subtitle items job_id=%s items=%s",
            job_id,
            len(source_items),
        )
        translated_items = translate_sentence_items(
            source_items,
            self.llm,
            source_language,
            target_language,
            analysis,
            work_dir,
        )
        write_json_artifact(work_dir, "07_translated_sentence_items.json", translated_items)
        await publisher.publish_progress(job_id, 70)

        logger.info("Building translated subtitles job_id=%s", job_id)
        subtitles = build_final_subtitles(translated_items, self.llm, target_language, work_dir)
        write_json_artifact(work_dir, "09_final_subtitles.json", subtitles)
        validate_subtitles(subtitles)
        vtt_content = write_webvtt(subtitles)
        write_text_artifact(work_dir, "10_final_subtitles.vtt", vtt_content)
        logger.info(
            "Uploading translated VTT job_id=%s cues=%s output_key=%s",
            job_id,
            len(subtitles),
            expected_output_file_key,
        )
        uploaded = await asyncio.to_thread(
            self.storage.upload_text,
            expected_output_file_key,
            vtt_content,
            CONTENT_TYPE_VTT,
        )
        await publisher.publish_progress(job_id, 90)
        await publisher.publish_completed(
            {
                "jobId": job_id,
                "outputFileKey": uploaded["fileKey"],
                "outputFileUrl": uploaded["fileUrl"],
                "fileName": f"{job_id}.vtt",
                "fileType": CONTENT_TYPE_VTT,
                "fileSize": uploaded["fileSize"],
                "languageCode": target_language,
                "displayName": event.get("displayName") or target_language,
                "wordConfidenceFileKey": None,
                "wordConfidenceFileUrl": None,
            }
        )
        logger.info("Translate subtitle job completed job_id=%s output_key=%s", job_id, uploaded["fileKey"])


def translate_sentence_items(
    source_items: list[SentenceItem],
    llm: DigitalOceanLLM,
    source_language: str,
    target_language: str,
    analysis: dict,
    artifact_dir: Path | None = None,
) -> list[SentenceItem]:
    translated_by_id: dict[int, str] = {}
    raw_items = [{"id": item.id, "text": item.text} for item in source_items]
    for batch_index, batch in enumerate(
        chunk_list(raw_items, settings.TRANSLATE_BATCH_SIZE),
        start=1,
    ):
        write_json_artifact(
            artifact_dir,
            f"06_llm_translate_batch_{batch_index}_input.json",
            batch,
        )
        translated_batch = llm.translate_batch(
            batch,
            source_language,
            target_language,
            analysis,
        )
        write_json_artifact(
            artifact_dir,
            f"06_llm_translate_batch_{batch_index}_output.json",
            translated_batch,
        )
        for translated in translated_batch:
            translated_by_id[int(translated["id"])] = translated["text"]
    return [
        SentenceItem(
            id=item.id,
            source_segment_id=item.source_segment_id,
            start=item.start,
            end=item.end,
            text=translated_by_id.get(item.id, item.text),
        )
        for item in source_items
    ]


def word_confidence_key(output_file_key: str) -> str:
    if output_file_key.endswith(".vtt"):
        return output_file_key[:-4] + ".words.json"
    return output_file_key + ".words.json"


def normalize_language(language_code: str) -> str:
    language_code = (language_code or "und").strip()
    return language_code if language_code else "und"


def require(event: dict, field: str) -> str:
    value = event.get(field)
    if value is None or str(value).strip() == "":
        raise ValueError(f"Missing required event field: {field}")
    return str(value)


def write_json_artifact(directory: Path | None, file_name: str, value) -> None:
    if directory is None:
        return
    try:
        directory.mkdir(parents=True, exist_ok=True)
        (directory / file_name).write_text(
            json.dumps(to_jsonable(value), ensure_ascii=False, indent=2),
            encoding="utf-8",
        )
    except Exception:
        logger.warning("Failed to write JSON artifact file=%s", file_name, exc_info=True)


def write_text_artifact(directory: Path | None, file_name: str, content: str) -> None:
    if directory is None:
        return
    try:
        directory.mkdir(parents=True, exist_ok=True)
        (directory / file_name).write_text(content, encoding="utf-8")
    except Exception:
        logger.warning("Failed to write text artifact file=%s", file_name, exc_info=True)
