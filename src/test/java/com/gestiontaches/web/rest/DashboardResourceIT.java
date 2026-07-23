package com.gestiontaches.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gestiontaches.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link DashboardResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
class DashboardResourceIT {

    @Autowired
    private MockMvc restDashboardMockMvc;

    @Test
    @WithMockUser(username = "admin", authorities = { "ROLE_ADMIN" })
    void getKpisAsAdmin() throws Exception {
        restDashboardMockMvc
            .perform(get("/api/dashboard/kpis").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.totalProjects").exists())
            .andExpect(jsonPath("$.totalTasks").exists())
            .andExpect(jsonPath("$.overdueTasks").exists());
    }

    @Test
    @WithMockUser(username = "manager", authorities = { "ROLE_PROJET_MANAGER", "ROLE_USER" })
    void getKpisAsManager() throws Exception {
        restDashboardMockMvc
            .perform(get("/api/dashboard/kpis").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.totalProjects").exists())
            .andExpect(jsonPath("$.totalTasks").exists())
            .andExpect(jsonPath("$.overdueTasks").exists());
    }
}
