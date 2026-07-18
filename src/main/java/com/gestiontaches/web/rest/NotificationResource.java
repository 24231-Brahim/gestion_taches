package com.gestiontaches.web.rest;

import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.NotificationService;
import com.gestiontaches.service.UserService;
import com.gestiontaches.service.dto.NotificationDTO;
import com.gestiontaches.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/notifications")
public class NotificationResource {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResource.class);

    private static final String ENTITY_NAME = "notification";

    @Value("${jhipster.clientApp.name:gestionTaches}")
    private String applicationName;

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationResource(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Long userId = getCurrentUserId();
        Pageable limited = pageable.isUnpaged()
            ? org.springframework.data.domain.PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("createdAt").descending())
            : pageable;
        Page<NotificationDTO> page = notificationService.findByUserId(userId, limited);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.countUnreadByUserId(userId));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable("id") Long id) {
        LOG.debug("REST request to mark Notification as read : {}", id);
        NotificationDTO dto = new NotificationDTO();
        dto.setId(id);
        dto.setIsRead(true);
        Optional<NotificationDTO> result = notificationService.partialUpdate(dto);
        return ResponseUtil.wrapOrNotFound(result);
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead() {
        LOG.debug("REST request to mark all notifications as read");
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new BadRequestAlertException("User not found", ENTITY_NAME, "usernotfound")
        );
        return userService
            .getUserWithAuthoritiesByLogin(login)
            .orElseThrow(() -> new BadRequestAlertException("User not found", ENTITY_NAME, "usernotfound"))
            .getId();
    }
}
