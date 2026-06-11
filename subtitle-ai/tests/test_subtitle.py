import os
import unittest


os.environ.setdefault("AWS_S3_BUCKET", "test-bucket")

from app.models import SentenceItem
from app.services.subtitle import (
    build_final_subtitles,
    distribute_time,
    format_vtt_timestamp,
    parse_webvtt,
    validate_subtitles,
    write_webvtt,
)


class NoopLlm:
    def split_subtitle_batch(self, items, target_language):
        return {}


class SubtitleTest(unittest.TestCase):
    def test_vtt_parse_and_write(self):
        cues = parse_webvtt(
            "WEBVTT\n\n"
            "00:00:01.000 --> 00:00:02.500\n"
            "Hello world\n\n"
            "00:00:03.000 --> 00:00:04.000\n"
            "Second cue\n"
        )
        self.assertEqual(2, len(cues))
        self.assertEqual("00:00:01.000", format_vtt_timestamp(cues[0].start))
        rendered = write_webvtt(cues)
        self.assertTrue(rendered.startswith("WEBVTT\n\n"))
        self.assertIn("00:00:03.000 --> 00:00:04.000", rendered)

    def test_distribute_time_preserves_bounds(self):
        result = distribute_time(10.0, 14.0, ["short", "a much longer chunk"])
        self.assertEqual(10.0, result[0]["start"])
        self.assertEqual(14.0, result[-1]["end"])
        self.assertGreaterEqual(result[1]["start"], result[0]["end"])

    def test_build_final_subtitles_fallback_split(self):
        item = SentenceItem(
            id=1,
            source_segment_id=1,
            start=0.0,
            end=8.0,
            text=" ".join(["word"] * 40),
        )
        subtitles = build_final_subtitles([item], NoopLlm(), "en")
        validate_subtitles(subtitles)
        self.assertGreater(len(subtitles), 1)


if __name__ == "__main__":
    unittest.main()
