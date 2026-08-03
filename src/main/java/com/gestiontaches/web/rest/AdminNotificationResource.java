package com.gestiontaches.web.rest;

import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.NotificationService;
import com.gestiontaches.service.dto.NotificationDTO;
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
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
public class AdminNotificationResource {

    private static final Logger LOG = LoggerFactory.getLogger(AdminNotificationResource.class);

    private final NotificationService notificationService;

    public AdminNotificationResource(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * {@code GET  /api/admin/notifications} : get all notifications across all users, most recent first.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all notifications.
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to get all Notifications");
        final Page<NotificationDTO> page = notificationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
