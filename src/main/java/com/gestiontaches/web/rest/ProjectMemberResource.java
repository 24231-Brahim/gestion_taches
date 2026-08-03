package com.gestiontaches.web.rest;

import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.ProjectMemberService;
import com.gestiontaches.service.dto.ProjectMemberDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/api/admin/project-members")
@PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
public class ProjectMemberResource {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectMemberResource.class);

    private final ProjectMemberService projectMemberService;

    public ProjectMemberResource(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    /**
     * {@code GET  /api/admin/project-members} : get all project members with their project and user information.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all project members.
     */
    @GetMapping
    public ResponseEntity<List<ProjectMemberDTO>> getAllProjectMembers(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to get all ProjectMembers");
        final Page<ProjectMemberDTO> page = projectMemberService.findAllWithProjectAndUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
