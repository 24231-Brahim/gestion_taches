package com.gestiontaches.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gestiontaches.IntegrationTest;
import com.gestiontaches.domain.*;
import com.gestiontaches.domain.enumeration.*;
import com.gestiontaches.repository.*;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.dto.*;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
class NotificationTriggersIT {

    @Autowired
    private TaskService taskService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager em;

    private User adminUser;
    private User devUser1;
    private User devUser2;
    private Project project;

    @BeforeEach
    void init() {
        adminUser = createUser("admin-trigger", AuthoritiesConstants.ADMIN);
        devUser1 = createUser("dev-trigger-1", AuthoritiesConstants.DEVELOPER);
        devUser2 = createUser("dev-trigger-2", AuthoritiesConstants.DEVELOPER);

        project = new Project();
        project.setName("Project Triggers");
        project.setKey("PTR" + (System.currentTimeMillis() % 10000));
        project.setCreatedAt(Instant.now());
        project.setOwner(adminUser);
        em.persist(project);
        em.flush();

        // Admin is OWNER by default when project is saved, let's add devUser1 and devUser2 as members
        setupSecurityContext(adminUser);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        notificationRepository.deleteAll();
        commentRepository.deleteAll();
        taskRepository.deleteAll();
        projectMemberRepository.deleteAll();
    }

    @Test
    void testTaskAssignmentNotification() {
        // Add devUser1 to project members
        projectService.addMember(project.getId(), devUser1.getId());
        notificationRepository.deleteAll(); // clear member add notification

        Task task = new Task();
        task.setTitle("Assignment Task");
        task.setStatus(TaskStatus.NEW);
        task.setPriority(Priority.MEDIUM);
        task.setProject(project);
        task.setCreatedBy(adminUser);
        task.setCreatedAt(Instant.now());
        em.persist(task);
        em.flush();

        // Assign to devUser1 (logged in as adminUser)
        taskService.assign(task.getId(), devUser1);

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser1.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getMessage()).contains("assigné");
        assertThat(notifications.get(0).getTaskTitle()).isEqualTo("Assignment Task");

        // Test non-auto-notification (devUser1 assigns a task to themselves - should not notify devUser1)
        setupSecurityContext(devUser1);
        notificationRepository.deleteAll();

        Task task2 = new Task();
        task2.setTitle("Self Assignment Task");
        task2.setStatus(TaskStatus.NEW);
        task2.setPriority(Priority.MEDIUM);
        task2.setProject(project);
        task2.setCreatedBy(devUser1);
        task2.setAssignee(devUser1);
        task2.setCreatedAt(Instant.now());
        em.persist(task2);
        em.flush();

        // Should not create notification
        assertThat(notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser1.getId())).isEmpty();
    }

    @Test
    void testTaskStatusChangeNotification() {
        projectService.addMember(project.getId(), devUser1.getId());

        Task task = new Task();
        task.setTitle("Status Change Task");
        task.setStatus(TaskStatus.NEW);
        task.setPriority(Priority.MEDIUM);
        task.setProject(project);
        task.setCreatedBy(adminUser);
        task.setAssignee(devUser1);
        task.setCreatedAt(Instant.now());
        em.persist(task);
        em.flush();

        notificationRepository.deleteAll();

        // Change status (logged in as adminUser)
        setupSecurityContext(adminUser);
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setStatus(TaskStatus.IN_PROGRESS);
        taskService.partialUpdate(dto);

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser1.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getMessage()).contains("statut");
    }

    @Test
    void testCommentCreatedNotification() {
        projectService.addMember(project.getId(), devUser1.getId());
        projectService.addMember(project.getId(), devUser2.getId());

        Task task = new Task();
        task.setTitle("Comment Task");
        task.setStatus(TaskStatus.NEW);
        task.setPriority(Priority.MEDIUM);
        task.setProject(project);
        task.setCreatedBy(devUser1); // Creator
        task.setAssignee(devUser2); // Assignee
        task.setCreatedAt(Instant.now());
        em.persist(task);
        em.flush();

        notificationRepository.deleteAll();

        // Logged in as adminUser (comment author)
        setupSecurityContext(adminUser);
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setContent("Looks good!");
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(task.getId());
        commentDTO.setTask(taskDTO);
        commentService.save(commentDTO);

        // Assignee and Creator should get notified (excluding adminUser because adminUser commented)
        List<Notification> dev1Notifs = notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser1.getId());
        List<Notification> dev2Notifs = notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser2.getId());

        assertThat(dev1Notifs).hasSize(1);
        assertThat(dev2Notifs).hasSize(1);
        assertThat(dev1Notifs.get(0).getMessage()).contains("commentaire");
    }

    @Test
    void testProjectMemberAddedNotification() {
        notificationRepository.deleteAll();

        // Add devUser1 to project
        projectService.addMember(project.getId(), devUser1.getId());

        List<Notification> notifications = notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser1.getId());
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getMessage()).contains("ajouté au projet");
    }

    @Test
    void testProjectDeletedNotification() {
        projectService.addMember(project.getId(), devUser1.getId());
        projectService.addMember(project.getId(), devUser2.getId());
        notificationRepository.deleteAll();

        // Admin deletes project
        projectService.delete(project.getId());

        // devUser1 and devUser2 must be notified, but not adminUser (who performed the delete)
        List<Notification> dev1Notifs = notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser1.getId());
        List<Notification> dev2Notifs = notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser2.getId());
        List<Notification> adminNotifs = notificationRepository.findByUser_idOrderByCreatedAtDesc(adminUser.getId());

        assertThat(dev1Notifs).hasSize(1);
        assertThat(dev1Notifs.get(0).getMessage()).contains("supprimé");
        assertThat(dev2Notifs).hasSize(1);
        assertThat(adminNotifs).isEmpty();
    }

    @Test
    void testSprintCreatedNotification() {
        projectService.addMember(project.getId(), devUser1.getId());
        projectService.addMember(project.getId(), devUser2.getId());
        notificationRepository.deleteAll();

        // Create sprint
        SprintDTO sprintDTO = new SprintDTO();
        sprintDTO.setName("Sprint Test");
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(project.getId());
        sprintDTO.setProject(projectDTO);

        sprintService.save(sprintDTO);

        List<Notification> dev1Notifs = notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser1.getId());
        List<Notification> dev2Notifs = notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser2.getId());

        assertThat(dev1Notifs).hasSize(1);
        assertThat(dev1Notifs.get(0).getMessage()).contains("sprint");
        assertThat(dev2Notifs).hasSize(1);
    }

    @Test
    void testNotificationPurge() {
        notificationRepository.deleteAll();

        // Old notification (16 days ago)
        Notification oldNotif = new Notification();
        oldNotif.setMessage("Old alert");
        oldNotif.setUser(devUser1);
        oldNotif.setIsRead(false);
        oldNotif.setCreatedAt(Instant.now().minus(16, ChronoUnit.DAYS));
        notificationRepository.save(oldNotif);

        // Recent notification (5 days ago)
        Notification recentNotif = new Notification();
        recentNotif.setMessage("Recent alert");
        recentNotif.setUser(devUser1);
        recentNotif.setIsRead(false);
        recentNotif.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));
        notificationRepository.save(recentNotif);

        em.flush();

        // Run purge
        notificationService.purgeOldNotifications();

        List<Notification> remaining = notificationRepository.findByUser_idOrderByCreatedAtDesc(devUser1.getId());
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getMessage()).isEqualTo("Recent alert");
    }

    private void setupSecurityContext(User user) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(user.getLogin());
        when(jwt.getClaim("userId")).thenReturn(user.getId());

        var authorities = List.of(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
        var auth = new UsernamePasswordAuthenticationToken(jwt, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private User createUser(String login, String authority) {
        User user = new User();
        user.setLogin(login);
        user.setPassword("A".repeat(60));
        user.setActivated(true);
        user.setEmail(login + "@localhost");
        user.setFirstName(login);
        user.setLastName("user");
        user.setLangKey("fr");
        Authority auth = new Authority();
        auth.setName(authority);
        user.setAuthorities(Set.of(auth));
        userRepository.saveAndFlush(user);
        return user;
    }
}
