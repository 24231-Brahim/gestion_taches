package com.gestiontaches.domain;

import static com.gestiontaches.domain.EpicTestSamples.*;
import static com.gestiontaches.domain.ProjectTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.gestiontaches.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EpicTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Epic.class);
        Epic epic1 = getEpicSample1();
        Epic epic2 = new Epic();
        assertThat(epic1).isNotEqualTo(epic2);

        epic2.setId(epic1.getId());
        assertThat(epic1).isEqualTo(epic2);

        epic2 = getEpicSample2();
        assertThat(epic1).isNotEqualTo(epic2);
    }

    @Test
    void projectTest() {
        Epic epic = getEpicRandomSampleGenerator();
        Project projectBack = getProjectRandomSampleGenerator();

        epic.setProject(projectBack);
        assertThat(epic.getProject()).isEqualTo(projectBack);

        epic.project(null);
        assertThat(epic.getProject()).isNull();
    }
}
