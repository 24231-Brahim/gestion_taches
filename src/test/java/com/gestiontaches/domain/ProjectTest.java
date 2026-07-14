package com.gestiontaches.domain;

import static com.gestiontaches.domain.EpicTestSamples.*;
import static com.gestiontaches.domain.ProjectTestSamples.*;
import static com.gestiontaches.domain.SprintTestSamples.*;
import static com.gestiontaches.domain.TaskTestSamples.*;
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
    void tasksTest() {
        Project project = getProjectRandomSampleGenerator();
        Task taskBack = getTaskRandomSampleGenerator();

        project.addTask(taskBack);
        assertThat(project.getTasks()).containsOnly(taskBack);
        assertThat(taskBack.getProject()).isEqualTo(project);

        project.removeTask(taskBack);
        assertThat(project.getTasks()).doesNotContain(taskBack);
        assertThat(taskBack.getProject()).isNull();

        project.tasks(new HashSet<>(Set.of(taskBack)));
        assertThat(project.getTasks()).containsOnly(taskBack);
        assertThat(taskBack.getProject()).isEqualTo(project);

        project.setTasks(new HashSet<>());
        assertThat(project.getTasks()).doesNotContain(taskBack);
        assertThat(taskBack.getProject()).isNull();
    }
}
