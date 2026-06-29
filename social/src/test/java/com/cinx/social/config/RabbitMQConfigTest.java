package com.cinx.social.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {
    @Test
    void declaresCourseQnaExchange() {
        RabbitMQConfig config = new RabbitMQConfig();

        assertThat(config.qnaExchange().getName()).isEqualTo("course.qna.exchange");
        assertThat(config.qnaExchange().isDurable()).isTrue();
    }
}
