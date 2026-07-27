package com.gestiontaches.web.rest;

import com.gestiontaches.domain.Project;
import com.gestiontaches.repository.EpicRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.SprintRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.SearchResultDTO;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SearchResource {

    private static final Logger LOG = LoggerFactory.getLogger(SearchResource.class);
    private static final int MAX_RESULTS_PER_TYPE = 10;

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final EpicRepository epicRepository;

    public SearchResource(
        ProjectRepository projectRepository,
        TaskRepository taskRepository,
        SprintRepository sprintRepository,
        EpicRepository epicRepository
    ) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
        this.epicRepository = epicRepository;
    }

    @GetMapping("/search")
    public List<SearchResultDTO> search(@RequestParam("q") String query) {
        LOG.debug("REST request to search for : {}", query);
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        String trimmed = query.trim();
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentLogin == null) {
            return List.of();
        }

        List<Project> userProjects = projectRepository.findAllByOwnerLoginOrMemberLogin(currentLogin);
        List<Long> projectIds = userProjects.stream().map(Project::getId).toList();

        List<SearchResultDTO> results = new ArrayList<>();

        List<Project> matchingProjects = projectRepository.searchByQuery(trimmed, currentLogin);
        for (Project p : matchingProjects.stream().limit(MAX_RESULTS_PER_TYPE).toList()) {
            results.add(
                new SearchResultDTO(
                    "project",
                    p.getId(),
                    p.getName(),
                    p.getDescription(),
                    p.getKey(),
                    null,
                    "/project/" + p.getKey() + "/view"
                )
            );
        }

        if (!projectIds.isEmpty()) {
            List<com.gestiontaches.domain.Task> matchingTasks = taskRepository.searchByQuery(trimmed, projectIds);
            for (com.gestiontaches.domain.Task t : matchingTasks.stream().limit(MAX_RESULTS_PER_TYPE).toList()) {
                String projectKey = t.getProject() != null ? t.getProject().getKey() : null;
                String status = t.getStatus() != null ? t.getStatus().name() : null;
                String link = projectKey != null ? "/project/" + projectKey + "/task/" + t.getId() + "/view" : null;
                results.add(new SearchResultDTO("task", t.getId(), t.getTitle(), t.getDescription(), projectKey, status, link));
            }

            List<com.gestiontaches.domain.Sprint> matchingSprints = sprintRepository.searchByQuery(trimmed, projectIds);
            for (com.gestiontaches.domain.Sprint s : matchingSprints.stream().limit(MAX_RESULTS_PER_TYPE).toList()) {
                String projectKey = s.getProject() != null ? s.getProject().getKey() : null;
                String status = s.getStatus() != null ? s.getStatus().name() : null;
                String link = projectKey != null ? "/project/" + projectKey + "/sprint/" + s.getId() + "/view" : null;
                results.add(new SearchResultDTO("sprint", s.getId(), s.getName(), s.getGoal(), projectKey, status, link));
            }

            List<com.gestiontaches.domain.Epic> matchingEpics = epicRepository.searchByQuery(trimmed, projectIds);
            for (com.gestiontaches.domain.Epic e : matchingEpics.stream().limit(MAX_RESULTS_PER_TYPE).toList()) {
                String projectKey = e.getProject() != null ? e.getProject().getKey() : null;
                String status = e.getStatus() != null ? e.getStatus().name() : null;
                String link = projectKey != null ? "/project/" + projectKey + "/epic/" + e.getId() + "/view" : null;
                results.add(new SearchResultDTO("epic", e.getId(), e.getTitle(), e.getDescription(), projectKey, status, link));
            }
        }

        return results;
    }
}
