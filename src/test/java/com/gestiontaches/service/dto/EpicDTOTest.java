package com.gestiontaches.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gestiontaches.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class EpicDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(EpicDTO.class);
        EpicDTO epicDTO1 = new EpicDTO();
        epicDTO1.setId(1L);
        EpicDTO epicDTO2 = new EpicDTO();
        assertThat(epicDTO1).isNotEqualTo(epicDTO2);
        epicDTO2.setId(epicDTO1.getId());
        assertThat(epicDTO1).isEqualTo(epicDTO2);
        epicDTO2.setId(2L);
        assertThat(epicDTO1).isNotEqualTo(epicDTO2);
        epicDTO1.setId(null);
        assertThat(epicDTO1).isNotEqualTo(epicDTO2);
    }
}
