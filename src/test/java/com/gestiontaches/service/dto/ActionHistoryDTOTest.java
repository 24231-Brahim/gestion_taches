package com.gestiontaches.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.gestiontaches.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ActionHistoryDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ActionHistoryDTO.class);
        ActionHistoryDTO actionHistoryDTO1 = new ActionHistoryDTO();
        actionHistoryDTO1.setId(1L);
        ActionHistoryDTO actionHistoryDTO2 = new ActionHistoryDTO();
        assertThat(actionHistoryDTO1).isNotEqualTo(actionHistoryDTO2);
        actionHistoryDTO2.setId(actionHistoryDTO1.getId());
        assertThat(actionHistoryDTO1).isEqualTo(actionHistoryDTO2);
        actionHistoryDTO2.setId(2L);
        assertThat(actionHistoryDTO1).isNotEqualTo(actionHistoryDTO2);
        actionHistoryDTO1.setId(null);
        assertThat(actionHistoryDTO1).isNotEqualTo(actionHistoryDTO2);
    }
}
