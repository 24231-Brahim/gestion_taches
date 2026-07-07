package com.gestiontaches.domain;

import static com.gestiontaches.domain.ActionHistoryTestSamples.*;
import static com.gestiontaches.domain.IssueTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.gestiontaches.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ActionHistoryTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ActionHistory.class);
        ActionHistory actionHistory1 = getActionHistorySample1();
        ActionHistory actionHistory2 = new ActionHistory();
        assertThat(actionHistory1).isNotEqualTo(actionHistory2);

        actionHistory2.setId(actionHistory1.getId());
        assertThat(actionHistory1).isEqualTo(actionHistory2);

        actionHistory2 = getActionHistorySample2();
        assertThat(actionHistory1).isNotEqualTo(actionHistory2);
    }

    @Test
    void issueTest() {
        ActionHistory actionHistory = getActionHistoryRandomSampleGenerator();
        Issue issueBack = getIssueRandomSampleGenerator();

        actionHistory.setIssue(issueBack);
        assertThat(actionHistory.getIssue()).isEqualTo(issueBack);

        actionHistory.issue(null);
        assertThat(actionHistory.getIssue()).isNull();
    }
}
