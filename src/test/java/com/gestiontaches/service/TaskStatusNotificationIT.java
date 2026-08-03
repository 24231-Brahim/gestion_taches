package com.gestiontaches.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.Authority;
import com.gestiontaches.domain.Notification;
import com.gestiontaches.domain.Project;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.User;
import com.gestiontaches.domain.enumeration.Priority;
import com.gestiontaches.domain.enumeration.TaskStatus;
import com.gestiontaches.repository.NotificationRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.TaskTransitionRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.dto.ProjectDTO;
import com.gestiontaches.service.dto.TaskDTO;
import com.gestiontaches.service.dto.UserDTO;
import com.gestiontaches.service.mapper.TaskMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class TaskStatusNotificationIT {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TaskTransitionRepository taskTransitionRepository;

    @Autowired
    private EntityManager em;

    private User adminCreator;
    private User adminAssignee;
    private User developerUser;
    private Project project;
    private Task task;
    private final Set<User> createdUsers = new HashSet<>();

    @BeforeEach
    void initTest() {
        adminCreator = createAdminUser("admin-creator");
        adminAssignee = createAdminUser("admin-assignee");
        developerUser = createDeveloperUser("dev-user");
        createdUsers.addAll(List.of(adminCreator, adminAssignee, developerUser));

        project = new Project();
        project.setName("Test Project");
        project.setKey("TP" + (System.currentTimeMillis() % 10000));
        project.setCreatedAt(Instant.now());
        project.setOwner(adminCreator);
        em.persist(project);
        em.flush();

        task = new Task();
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setPriority(Priority.MEDIUM);
        task.setCreatedAt(Instant.now());
        task.setProject(project);
        task.setCreatedBy(adminCreator);
        em.persist(task);
        em.flush();

        setupSecurityContext(adminCreator);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        em.flush();
        taskTransitionRepository.deleteAll();
        notificationRepository.deleteAll();
        taskRepository.deleteAll();
        em.flush();
    }

    @Test
    void creatorIsAdmin_statusDone_createsNotificationForCreator() {
        TaskDTO taskDTO = taskMapper.toDto(task);
        taskDTO.setStatus(TaskStatus.DONE);

        taskService.update(taskDTO);

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getMessage()).contains("Terminée");
        assertThat(notifications.get(0).getMessage()).contains("créée");
        assertThat(notifications.get(0).getTaskTitle()).isEqualTo("Test Task");
        assertThat(notifications.get(0).getIsRead()).isFalse();
    }

    @Test
    void creatorIsAdmin_statusCancelled_createsNotificationForCreator() {
        TaskDTO taskDTO = taskMapper.toDto(task);
        taskDTO.setStatus(TaskStatus.CANCELLED);

        taskService.update(taskDTO);

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getMessage()).contains("Annulée");
        assertThat(notifications.get(0).getMessage()).contains("créée");
    }

    @Test
    void assigneeIsAdmin_differentFromCreator_createsNotificationForAssignee() {
        task.setAssignee(adminAssignee);
        em.persist(task);
        em.flush();

        TaskDTO taskDTO = taskMapper.toDto(task);
        taskDTO.setStatus(TaskStatus.DONE);

        taskService.update(taskDTO);

        List<Notification> assigneeNotifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminAssignee.getId());
        assertThat(assigneeNotifications).hasSize(1);
        assertThat(assigneeNotifications.get(0).getMessage()).contains("Terminée");
        assertThat(assigneeNotifications.get(0).getMessage()).contains("assignée");
    }

    @Test
    void bothCreatorAndAssigneeAreAdmin_differentUsers_createsTwoNotifications() {
        task.setAssignee(adminAssignee);
        em.persist(task);
        em.flush();

        TaskDTO taskDTO = taskMapper.toDto(task);
        taskDTO.setStatus(TaskStatus.DONE);

        taskService.update(taskDTO);

        List<Notification> creatorNotifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        List<Notification> assigneeNotifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminAssignee.getId());

        assertThat(creatorNotifications).hasSize(1);
        assertThat(creatorNotifications.get(0).getMessage()).contains("créée");

        assertThat(assigneeNotifications).hasSize(1);
        assertThat(assigneeNotifications.get(0).getMessage()).contains("assignée");
    }

    @Test
    void creatorAndAssigneeAreSameAdmin_createsOnlyOneNotification() {
        task.setAssignee(adminCreator);
        em.persist(task);
        em.flush();

        TaskDTO taskDTO = taskMapper.toDto(task);
        taskDTO.setStatus(TaskStatus.CANCELLED);

        taskService.update(taskDTO);

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getMessage()).contains("créée");
    }

    @Test
    void nonAdminCreatorAndAssignee_createsNotificationOnStatusChange() {
        task.setCreatedBy(developerUser);
        task.setAssignee(developerUser);
        em.persist(task);
        em.flush();

        TaskDTO taskDTO = taskMapper.toDto(task);
        taskDTO.setStatus(TaskStatus.DONE);

        taskService.update(taskDTO);

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(developerUser.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getMessage()).contains("Terminée");
    }

    @Test
    void taskReopenedThenClosedAgain_createsSecondNotification() {
        TaskDTO taskDTO = taskMapper.toDto(task);
        taskDTO.setStatus(TaskStatus.DONE);
        taskService.update(taskDTO);

        List<Notification> afterFirstDone = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(afterFirstDone).hasSize(1);

        Task refreshedTask = taskRepository.findById(task.getId()).orElseThrow();
        TaskDTO reopenDto = taskMapper.toDto(refreshedTask);
        reopenDto.setStatus(TaskStatus.IN_PROGRESS);
        taskService.update(reopenDto);

        List<Notification> afterReopen = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(afterReopen).hasSize(2);

        refreshedTask = taskRepository.findById(task.getId()).orElseThrow();
        TaskDTO secondDoneDto = taskMapper.toDto(refreshedTask);
        secondDoneDto.setStatus(TaskStatus.DONE);
        taskService.update(secondDoneDto);

        List<Notification> afterSecondDone = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(afterSecondDone).hasSize(3);
    }

    @Test
    void partialUpdate_statusDone_createsNotification() {
        TaskDTO partialDto = new TaskDTO();
        partialDto.setId(task.getId());
        partialDto.setStatus(TaskStatus.DONE);

        taskService.partialUpdate(partialDto);

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getMessage()).contains("Terminée");
        assertThat(notifications.get(0).getMessage()).contains("créée");
    }

    @Test
    void partialUpdate_otherStatusChange_createsNotification() {
        TaskDTO partialDto = new TaskDTO();
        partialDto.setId(task.getId());
        partialDto.setStatus(TaskStatus.IN_REVIEW);

        taskService.partialUpdate(partialDto);

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getMessage()).contains("En revue");
    }

    @Test
    void notificationHasCorrectTaskLink() {
        TaskDTO taskDTO = taskMapper.toDto(task);
        taskDTO.setStatus(TaskStatus.DONE);

        taskService.update(taskDTO);

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getTask()).isNotNull();
        assertThat(notifications.get(0).getTask().getId()).isEqualTo(task.getId());
    }

    @Test
    void taskCreation_withAssignee_createsNotificationForAssigneeOnly() {
        setupJwtSecurityContext(adminCreator);

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("New Task");
        taskDTO.setStatus(TaskStatus.NEW);
        taskDTO.setPriority(Priority.MEDIUM);
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(project.getId());
        taskDTO.setProject(projectDTO);
        UserDTO assigneeDTO = new UserDTO();
        assigneeDTO.setId(adminAssignee.getId());
        taskDTO.setAssignee(assigneeDTO);

        taskService.save(taskDTO);

        List<Notification> assigneeNotifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminAssignee.getId());
        assertThat(assigneeNotifications).hasSize(1);
        assertThat(assigneeNotifications.get(0).getMessage()).contains("assigné");
        assertThat(assigneeNotifications.get(0).getTaskTitle()).isEqualTo("New Task");

        List<Notification> creatorNotifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(creatorNotifications).isEmpty();
    }

    @Test
    void taskCreation_assignedToCurrentUser_createsNoNotification() {
        setupJwtSecurityContext(adminCreator);

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("Self Assigned Task");
        taskDTO.setStatus(TaskStatus.NEW);
        taskDTO.setPriority(Priority.MEDIUM);
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(project.getId());
        taskDTO.setProject(projectDTO);
        UserDTO assigneeDTO = new UserDTO();
        assigneeDTO.setId(adminCreator.getId());
        taskDTO.setAssignee(assigneeDTO);

        taskService.save(taskDTO);

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminCreator.getId());
        assertThat(notifications).isEmpty();
    }

    private void setupJwtSecurityContext(User user) {
        var now = Instant.now();
        var jwt = Jwt.withTokenValue("token")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(60))
            .subject(user.getLogin())
            .claim("userId", user.getId())
            .header("alg", "HS512")
            .build();
        var authorities = List.of(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
        var auth = new UsernamePasswordAuthenticationToken(jwt, "token", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void setupSecurityContext(User user) {
        ClaimAccessor principal = mock(ClaimAccessor.class);
        when(principal.getClaim("userId")).thenReturn(user.getId());

        var authorities = List.of(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private User createAdminUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setPassword("A".repeat(60));
        user.setActivated(true);
        user.setEmail(login + "@localhost");
        user.setFirstName(login);
        user.setLastName("user");
        user.setLangKey("fr");
        Authority adminAuth = new Authority();
        adminAuth.setName(AuthoritiesConstants.ADMIN);
        user.setAuthorities(Set.of(adminAuth));
        userRepository.saveAndFlush(user);
        return user;
    }

    private User createDeveloperUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setPassword("A".repeat(60));
        user.setActivated(true);
        user.setEmail(login + "@localhost");
        user.setFirstName(login);
        user.setLastName("user");
        user.setLangKey("fr");
        Authority devAuth = new Authority();
        devAuth.setName(AuthoritiesConstants.DEVELOPER);
        user.setAuthorities(Set.of(devAuth));
        userRepository.saveAndFlush(user);
        return user;
    }
}
