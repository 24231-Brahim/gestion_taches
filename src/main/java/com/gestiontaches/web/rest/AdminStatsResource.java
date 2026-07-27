package com.gestiontaches.web.rest;

import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
public class AdminStatsResource {

    private static final Logger LOG = LoggerFactory.getLogger(AdminStatsResource.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public AdminStatsResource(UserRepository userRepository, ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        LOG.debug("REST request to get admin stats");
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalProjects", projectRepository.count());
        stats.put("totalTasks", taskRepository.count());

        Map<String, Long> usersByRole = new LinkedHashMap<>();
        for (String role : new String[] {
            AuthoritiesConstants.ADMIN,
            AuthoritiesConstants.PROJET_MANAGER,
            AuthoritiesConstants.DEVELOPER,
            AuthoritiesConstants.USER,
        }) {
            long count = userRepository.findAllByAuthorityNames(java.util.List.of(role)).size();
            usersByRole.put(role, count);
        }
        stats.put("usersByRole", usersByRole);

        return stats;
    }
}
