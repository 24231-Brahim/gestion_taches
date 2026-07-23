package com.gestiontaches.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.Authority;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.UserService;
import jakarta.persistence.EntityManager;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link UserResource} admin user detail endpoint.
 */
@AutoConfigureMockMvc
@IntegrationTest
class UserAdminDetailResourceIT {

    private static final String TEST_LOGIN = "testdetailuser";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restUserMockMvc;

    private User testUser;

    @BeforeEach
    void initTest() {
        testUser = new User();
        testUser.setLogin(TEST_LOGIN);
        testUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        testUser.setActivated(true);
        testUser.setEmail("testdetail@localhost");
        testUser.setFirstName("Test");
        testUser.setLastName("Detail");
        testUser.setImageUrl("http://placehold.it/50x50");
        testUser.setLangKey("en");
        Authority userAuth = new Authority();
        userAuth.setName(AuthoritiesConstants.USER);
        testUser.setAuthorities(Set.of(userAuth));
        userRepository.saveAndFlush(testUser);
    }

    @AfterEach
    void cleanup() {
        userService.deleteUser(TEST_LOGIN);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void getUserDetailAsAdmin() throws Exception {
        restUserMockMvc
            .perform(get("/api/admin/users/{login}/detail", TEST_LOGIN).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.login").value(TEST_LOGIN))
            .andExpect(jsonPath("$.firstName").value("Test"))
            .andExpect(jsonPath("$.lastName").value("Detail"))
            .andExpect(jsonPath("$.email").value("testdetail@localhost"))
            .andExpect(jsonPath("$.activated").value(true))
            .andExpect(jsonPath("$.authorities").isArray());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = AuthoritiesConstants.DEVELOPER)
    void getUserDetailAsDeveloperShouldBeForbidden() throws Exception {
        restUserMockMvc
            .perform(get("/api/admin/users/{login}/detail", TEST_LOGIN).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = AuthoritiesConstants.USER)
    void getUserDetailAsUserShouldBeForbidden() throws Exception {
        restUserMockMvc
            .perform(get("/api/admin/users/{login}/detail", TEST_LOGIN).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = AuthoritiesConstants.ADMIN)
    void getUserDetailForNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/admin/users/{login}/detail", "unknownlogin")).andExpect(status().isNotFound());
    }
}
