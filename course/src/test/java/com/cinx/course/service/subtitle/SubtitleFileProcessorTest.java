package com.cinx.course.service.subtitle;

import com.cinx.common.exception.BadRequestException;
import com.cinx.course.consts.SubtitleFormat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubtitleFileProcessorTest {
    private final SubtitleFileProcessor processor = new SubtitleFileProcessor();

    @Test
    void normalizeToWebVtt_acceptsValidVtt() {
        String content = """
                WEBVTT

                00:00:01.000 --> 00:00:03.000
                Hello
                """;

        String result = processor.normalizeToWebVtt(SubtitleFormat.VTT, content);

        assertThat(result).startsWith("WEBVTT");
        assertThat(result).contains("00:00:01.000 --> 00:00:03.000");
    }

    @Test
    void normalizeToWebVtt_convertsSrtToVtt() {
        String content = """
                1
                00:00:01,000 --> 00:00:03,000
                Hello
                """;

        String result = processor.normalizeToWebVtt(SubtitleFormat.SRT, content);

        assertThat(result).startsWith("WEBVTT");
        assertThat(result).contains("00:00:01.000 --> 00:00:03.000");
        assertThat(result).doesNotContain("00:00:01,000");
    }

    @Test
    void normalizeToWebVtt_rejectsMalformedVtt() {
        assertThatThrownBy(() -> processor.normalizeToWebVtt(SubtitleFormat.VTT, "hello"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("WEBVTT");
    }

    @Test
    void detectFormat_rejectsUnsupportedFileType() {
        assertThatThrownBy(() -> processor.detectFormat("intro.txt", "text/plain"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining(".vtt and .srt");
    }
}
