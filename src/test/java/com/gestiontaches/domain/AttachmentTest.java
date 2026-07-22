package com.gestiontaches.domain;

import static com.gestiontaches.domain.AttachmentTestSamples.*;
import static com.gestiontaches.domain.TaskTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.gestiontaches.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AttachmentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Attachment.class);
        Attachment attachment1 = getAttachmentSample1();
        Attachment attachment2 = new Attachment();
        assertThat(attachment1).isNotEqualTo(attachment2);

        attachment2.setId(attachment1.getId());
        assertThat(attachment1).isEqualTo(attachment2);

        attachment2 = getAttachmentSample2();
        assertThat(attachment1).isNotEqualTo(attachment2);
    }

    @Test
    void taskTest() {
        Attachment attachment = getAttachmentRandomSampleGenerator();
        Task taskBack = getTaskRandomSampleGenerator();

        attachment.setTask(taskBack);
        assertThat(attachment.getTask()).isEqualTo(taskBack);

        attachment.task(null);
        assertThat(attachment.getTask()).isNull();
    }
}
