package com.gestiontaches.domain;

import static com.gestiontaches.domain.ActionHistoryTestSamples.*;
import static com.gestiontaches.domain.AttachmentTestSamples.*;
import static com.gestiontaches.domain.CommentTestSamples.*;
import static com.gestiontaches.domain.EpicTestSamples.*;
import static com.gestiontaches.domain.IssueTestSamples.*;
import static com.gestiontaches.domain.ProjectTestSamples.*;
import static com.gestiontaches.domain.SprintTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.gestiontaches.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IssueTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Issue.class);
        Issue issue1 = getIssueSample1();
        Issue issue2 = new Issue();
        assertThat(issue1).isNotEqualTo(issue2);

        issue2.setId(issue1.getId());
        assertThat(issue1).isEqualTo(issue2);

        issue2 = getIssueSample2();
        assertThat(issue1).isNotEqualTo(issue2);
    }

    @Test
    void commentsTest() {
        Issue issue = getIssueRandomSampleGenerator();
        Comment commentBack = getCommentRandomSampleGenerator();

        issue.addComments(commentBack);
        assertThat(issue.getCommentses()).containsOnly(commentBack);
        assertThat(commentBack.getIssue()).isEqualTo(issue);

        issue.removeComments(commentBack);
        assertThat(issue.getCommentses()).doesNotContain(commentBack);
        assertThat(commentBack.getIssue()).isNull();

        issue.commentses(new HashSet<>(Set.of(commentBack)));
        assertThat(issue.getCommentses()).containsOnly(commentBack);
        assertThat(commentBack.getIssue()).isEqualTo(issue);

        issue.setCommentses(new HashSet<>());
        assertThat(issue.getCommentses()).doesNotContain(commentBack);
        assertThat(commentBack.getIssue()).isNull();
    }

    @Test
    void attachmentsTest() {
        Issue issue = getIssueRandomSampleGenerator();
        Attachment attachmentBack = getAttachmentRandomSampleGenerator();

        issue.addAttachments(attachmentBack);
        assertThat(issue.getAttachmentses()).containsOnly(attachmentBack);
        assertThat(attachmentBack.getIssue()).isEqualTo(issue);

        issue.removeAttachments(attachmentBack);
        assertThat(issue.getAttachmentses()).doesNotContain(attachmentBack);
        assertThat(attachmentBack.getIssue()).isNull();

        issue.attachmentses(new HashSet<>(Set.of(attachmentBack)));
        assertThat(issue.getAttachmentses()).containsOnly(attachmentBack);
        assertThat(attachmentBack.getIssue()).isEqualTo(issue);

        issue.setAttachmentses(new HashSet<>());
        assertThat(issue.getAttachmentses()).doesNotContain(attachmentBack);
        assertThat(attachmentBack.getIssue()).isNull();
    }

    @Test
    void historyTest() {
        Issue issue = getIssueRandomSampleGenerator();
        ActionHistory actionHistoryBack = getActionHistoryRandomSampleGenerator();

        issue.addHistory(actionHistoryBack);
        assertThat(issue.getHistories()).containsOnly(actionHistoryBack);
        assertThat(actionHistoryBack.getIssue()).isEqualTo(issue);

        issue.removeHistory(actionHistoryBack);
        assertThat(issue.getHistories()).doesNotContain(actionHistoryBack);
        assertThat(actionHistoryBack.getIssue()).isNull();

        issue.histories(new HashSet<>(Set.of(actionHistoryBack)));
        assertThat(issue.getHistories()).containsOnly(actionHistoryBack);
        assertThat(actionHistoryBack.getIssue()).isEqualTo(issue);

        issue.setHistories(new HashSet<>());
        assertThat(issue.getHistories()).doesNotContain(actionHistoryBack);
        assertThat(actionHistoryBack.getIssue()).isNull();
    }

    @Test
    void sprintTest() {
        Issue issue = getIssueRandomSampleGenerator();
        Sprint sprintBack = getSprintRandomSampleGenerator();

        issue.setSprint(sprintBack);
        assertThat(issue.getSprint()).isEqualTo(sprintBack);

        issue.sprint(null);
        assertThat(issue.getSprint()).isNull();
    }

    @Test
    void epicTest() {
        Issue issue = getIssueRandomSampleGenerator();
        Epic epicBack = getEpicRandomSampleGenerator();

        issue.setEpic(epicBack);
        assertThat(issue.getEpic()).isEqualTo(epicBack);

        issue.epic(null);
        assertThat(issue.getEpic()).isNull();
    }

    @Test
    void projectTest() {
        Issue issue = getIssueRandomSampleGenerator();
        Project projectBack = getProjectRandomSampleGenerator();

        issue.setProject(projectBack);
        assertThat(issue.getProject()).isEqualTo(projectBack);

        issue.project(null);
        assertThat(issue.getProject()).isNull();
    }
}
