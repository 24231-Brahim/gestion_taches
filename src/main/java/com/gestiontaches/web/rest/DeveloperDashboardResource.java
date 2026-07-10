package com.gestiontaches.web.rest;

import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.service.DeveloperDashboardService;
import com.gestiontaches.service.dto.DeveloperDashboardStatisticsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/developer-dashboard")
public class DeveloperDashboardResource {

    private final DeveloperDashboardService developerDashboardService;

    public DeveloperDashboardResource(DeveloperDashboardService developerDashboardService) {
        this.developerDashboardService = developerDashboardService;
    }

    @GetMapping("/statistics")
    @PreAuthorize(
        "hasAnyAuthority('" +
            AuthoritiesConstants.ADMIN +
            "', '" +
            AuthoritiesConstants.PROJET_MANAGER +
            "', '" +
            AuthoritiesConstants.DEVELOPER +
            "')"
    )
    public ResponseEntity<DeveloperDashboardStatisticsDTO> getStatistics() {
        return ResponseEntity.ok(developerDashboardService.getStatistics());
    }
}
