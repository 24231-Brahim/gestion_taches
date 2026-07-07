package com.gestiontaches.domain;

import static com.gestiontaches.domain.EpicTestSamples.*;
import static com.gestiontaches.domain.IssueTestSamples.*;
import static com.gestiontaches.domain.ProjectTestSamples.*;
import static com.gestiontaches.domain.SprintTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.gestiontaches.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Project.class);
        Project project1 = getProjectSample1();
        Project project2 = new Project();
        assertThat(project1).isNotEqualTo(project2);

        project2.setId(project1.getId());
        assertThat(project1).isEqualTo(project2);

        project2 = getProjectSample2();
        assertThat(project1).isNotEqualTo(project2);
    }

    @Test
    void sprintsTest() {
        Project project = getProjectRandomSampleGenerator();
        Sprint sprintBack = getSprintRandomSampleGenerator();

        project.addSprints(sprintBack);
        assertThat(project.getSprintses()).containsOnly(sprintBack);
        assertThat(sprintBack.getProject()).isEqualTo(project);

        project.removeSprints(sprintBack);
        assertThat(project.getSprintses()).doesNotContain(sprintBack);
        assertThat(sprintBack.getProject()).isNull();

        project.sprintses(new HashSet<>(Set.of(sprintBack)));
        assertThat(project.getSprintses()).containsOnly(sprintBack);
        assertThat(sprintBack.getProject()).isEqualTo(project);

        project.setSprintses(new HashSet<>());
        assertThat(project.getSprintses()).doesNotContain(sprintBack);
        assertThat(sprintBack.getProject()).isNull();
    }

    @Test
    void epicsTest() {
        Project project = getProjectRandomSampleGenerator();
        Epic epicBack = getEpicRandomSampleGenerator();

        project.addEpics(epicBack);
        assertThat(project.getEpicses()).containsOnly(epicBack);
        assertThat(epicBack.getProject()).isEqualTo(project);

        project.removeEpics(epicBack);
        assertThat(project.getEpicses()).doesNotContain(epicBack);
        assertThat(epicBack.getProject()).isNull();

        project.epicses(new HashSet<>(Set.of(epicBack)));
        assertThat(project.getEpicses()).containsOnly(epicBack);
        assertThat(epicBack.getProject()).isEqualTo(project);

        project.setEpicses(new HashSet<>());
        assertThat(project.getEpicses()).doesNotContain(epicBack);
        assertThat(epicBack.getProject()).isNull();
    }

    @Test
    void issuesTest() {
        Project project = getProjectRandomSampleGenerator();
        Issue issueBack = getIssueRandomSampleGenerator();

        project.addIssues(issueBack);
        assertThat(project.getIssueses()).containsOnly(issueBack);
        assertThat(issueBack.getProject()).isEqualTo(project);

        project.removeIssues(issueBack);
        assertThat(project.getIssueses()).doesNotContain(issueBack);
        assertThat(issueBack.getProject()).isNull();

        project.issueses(new HashSet<>(Set.of(issueBack)));
        assertThat(project.getIssueses()).containsOnly(issueBack);
        assertThat(issueBack.getProject()).isEqualTo(project);

        project.setIssueses(new HashSet<>());
        assertThat(project.getIssueses()).doesNotContain(issueBack);
        assertThat(issueBack.getProject()).isNull();
    }
}
