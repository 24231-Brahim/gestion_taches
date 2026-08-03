package com.gestiontaches.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gestiontaches.IntegrationTest;
import com.gestiontaches.security.AuthoritiesConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProjectMemberResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
class ProjectMemberResourceIT {

    private static final String ENTITY_API_URL = "/api/admin/project-members";

    @Autowired
    private MockMvc restProjectMemberMockMvc;

    @Test
    @Transactional
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void getAllProjectMembers_asUser_shouldForbid() throws Exception {
        restProjectMemberMockMvc.perform(get(ENTITY_API_URL)).andExpect(status().isForbidden());
    }
}
