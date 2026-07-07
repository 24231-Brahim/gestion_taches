package com.gestiontaches.domain;

import static com.gestiontaches.domain.CommentTestSamples.*;
import static com.gestiontaches.domain.IssueTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.gestiontaches.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CommentTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Comment.class);
        Comment comment1 = getCommentSample1();
        Comment comment2 = new Comment();
        assertThat(comment1).isNotEqualTo(comment2);

        comment2.setId(comment1.getId());
        assertThat(comment1).isEqualTo(comment2);

        comment2 = getCommentSample2();
        assertThat(comment1).isNotEqualTo(comment2);
    }

    @Test
    void issueTest() {
        Comment comment = getCommentRandomSampleGenerator();
        Issue issueBack = getIssueRandomSampleGenerator();

        comment.setIssue(issueBack);
        assertThat(comment.getIssue()).isEqualTo(issueBack);

        comment.issue(null);
        assertThat(comment.getIssue()).isNull();
    }
}
