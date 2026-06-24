package com.gestiontaches.domain;

import static com.gestiontaches.domain.AttachmentTestSamples.*;
import static com.gestiontaches.domain.IssueTestSamples.*;
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
    void issueTest() {
        Attachment attachment = getAttachmentRandomSampleGenerator();
        Issue issueBack = getIssueRandomSampleGenerator();

        attachment.setIssue(issueBack);
        assertThat(attachment.getIssue()).isEqualTo(issueBack);

        attachment.issue(null);
        assertThat(attachment.getIssue()).isNull();
    }
}
