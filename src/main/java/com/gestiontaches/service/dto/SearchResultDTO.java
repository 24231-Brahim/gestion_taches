package com.gestiontaches.service.dto;

import java.io.Serializable;

public class SearchResultDTO implements Serializable {

    private String type;
    private Long id;
    private String title;
    private String description;
    private String projectKey;
    private String status;
    private String link;

    public SearchResultDTO() {}

    public SearchResultDTO(String type, Long id, String title, String description, String projectKey, String status, String link) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.description = description;
        this.projectKey = projectKey;
        this.status = status;
        this.link = link;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
