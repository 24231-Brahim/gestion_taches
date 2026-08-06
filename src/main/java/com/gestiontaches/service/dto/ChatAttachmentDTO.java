package com.gestiontaches.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.gestiontaches.domain.ChatAttachment} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChatAttachmentDTO implements Serializable {

    private Long id;

    private Long messageId;

    @Size(max = 255)
    private String fileName;

    @Size(max = 100)
    private String fileType;

    private Long fileSize;

    @Size(max = 500)
    private String filePath;

    private String uploadedByLogin;

    @NotNull
    private Instant uploadedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUploadedByLogin() {
        return uploadedByLogin;
    }

    public void setUploadedByLogin(String uploadedByLogin) {
        this.uploadedByLogin = uploadedByLogin;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatAttachmentDTO)) {
            return false;
        }

        ChatAttachmentDTO that = (ChatAttachmentDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ChatAttachmentDTO{" + "id=" + getId() + ", fileName='" + getFileName() + "'" + "}";
    }
}
