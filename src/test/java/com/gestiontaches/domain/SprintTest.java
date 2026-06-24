package com.gestiontaches.domain;

import static com.gestiontaches.domain.ProjectTestSamples.*;
import static com.gestiontaches.domain.SprintTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.gestiontaches.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SprintTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Sprint.class);
        Sprint sprint1 = getSprintSample1();
        Sprint sprint2 = new Sprint();
        assertThat(sprint1).isNotEqualTo(sprint2);

        sprint2.setId(sprint1.getId());
        assertThat(sprint1).isEqualTo(sprint2);

        sprint2 = getSprintSample2();
        assertThat(sprint1).isNotEqualTo(sprint2);
    }

    @Test
    void projectTest() {
        Sprint sprint = getSprintRandomSampleGenerator();
        Project projectBack = getProjectRandomSampleGenerator();

        sprint.setProject(projectBack);
        assertThat(sprint.getProject()).isEqualTo(projectBack);

        sprint.project(null);
        assertThat(sprint.getProject()).isNull();
    }
}
