package com.gestiontaches.web.rest;

import com.gestiontaches.domain.Issue;
import com.gestiontaches.domain.enumeration.ProjectRole;
import com.gestiontaches.repository.IssueRepository;
import com.gestiontaches.repository.ProjectRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.service.ProjectPermissionService;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export/csv")
public class CsvExportResource {

    private static final Logger LOG = LoggerFactory.getLogger(CsvExportResource.class);

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final ProjectPermissionService projectPermissionService;

    public CsvExportResource(
        ProjectRepository projectRepository,
        IssueRepository issueRepository,
        UserRepository userRepository,
        ProjectPermissionService projectPermissionService
    ) {
        this.projectRepository = projectRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.projectPermissionService = projectPermissionService;
    }

    @GetMapping("/projects")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportProjectsCsv() {
        LOG.debug("REST request to export Projects as CSV");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("ID,Key,Name,Description,CreatedAt,OwnerLogin");
        projectRepository.findAll().forEach(p -> {
            String ownerLogin = p.getOwner() != null ? escapeCsv(p.getOwner().getLogin()) : "";
            pw.println(
                p.getId() +
                    "," +
                    escapeCsv(p.getKey()) +
                    "," +
                    escapeCsv(p.getName()) +
                    "," +
                    escapeCsv(p.getDescription()) +
                    "," +
                    p.getCreatedAt() +
                    "," +
                    ownerLogin
            );
        });
        return csvResponse(sw.toString(), "projects.csv");
    }

    @GetMapping("/issues")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportIssuesCsv() {
        LOG.debug("REST request to export Issues as CSV");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("ID,Title,Type,Status,Priority,ProjectKey,SprintName,EpicTitle,AssigneeLogin,CreatedAt");
        issueRepository.findAll().forEach(i -> {
            String projectKey = i.getProject() != null ? escapeCsv(i.getProject().getKey()) : "";
            String sprintName = i.getSprint() != null ? escapeCsv(i.getSprint().getName()) : "";
            String epicTitle = i.getEpic() != null ? escapeCsv(i.getEpic().getTitle()) : "";
            String assigneeLogin = i.getAssignee() != null ? escapeCsv(i.getAssignee().getLogin()) : "";
            pw.println(
                i.getId() +
                    "," +
                    escapeCsv(i.getTitle()) +
                    "," +
                    i.getType() +
                    "," +
                    i.getStatus() +
                    "," +
                    i.getPriority() +
                    "," +
                    projectKey +
                    "," +
                    sprintName +
                    "," +
                    epicTitle +
                    "," +
                    assigneeLogin +
                    "," +
                    i.getCreatedAt()
            );
        });
        return csvResponse(sw.toString(), "issues.csv");
    }

    @GetMapping("/projects/{projectId}/issues")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> exportProjectIssuesCsv(@PathVariable("projectId") Long projectId) {
        LOG.debug("REST request to export Issues of Project {} as CSV", projectId);
        projectPermissionService.requireProjectRole(projectId, ProjectRole.OWNER, ProjectRole.MANAGER);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("ID,Title,Type,Status,Priority,AssigneeLogin,CreatedAt,UpdatedAt");
        List<Issue> issues = issueRepository.findAllByProjectIdWithToOneRelationships(projectId);
        for (Issue i : issues) {
            String assigneeLogin = i.getAssignee() != null ? escapeCsv(i.getAssignee().getLogin()) : "";
            pw.println(
                i.getId() +
                    "," +
                    escapeCsv(i.getTitle()) +
                    "," +
                    i.getType() +
                    "," +
                    i.getStatus() +
                    "," +
                    i.getPriority() +
                    "," +
                    assigneeLogin +
                    "," +
                    i.getCreatedAt() +
                    "," +
                    i.getUpdatedAt()
            );
        }
        return csvResponse(sw.toString(), "project-" + projectId + "-issues.csv");
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority(\"" + com.gestiontaches.security.AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<byte[]> exportUsersCsv() {
        LOG.debug("REST request to export Users as CSV");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("ID,Login,FirstName,LastName,Email,Activated,LangKey,CreatedBy,CreatedDate");
        userRepository.findAll().forEach(u -> {
            pw.println(
                u.getId() +
                    "," +
                    escapeCsv(u.getLogin()) +
                    "," +
                    escapeCsv(u.getFirstName()) +
                    "," +
                    escapeCsv(u.getLastName()) +
                    "," +
                    escapeCsv(u.getEmail()) +
                    "," +
                    u.isActivated() +
                    "," +
                    escapeCsv(u.getLangKey()) +
                    "," +
                    escapeCsv(u.getCreatedBy()) +
                    "," +
                    u.getCreatedDate()
            );
        });
        return csvResponse(sw.toString(), "users.csv");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private ResponseEntity<byte[]> csvResponse(String csv, String filename) {
        byte[] bytes = csv.getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
