package com.gestiontaches.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A ChatAttachment.
 *
 * <p>Metadata of a file attached to a {@link ChatMessage} (image, PDF, document...).
 * Persisted for the future attachment/upload feature; no upload endpoint yet.</p>
 */
@Entity
@Table(name = "chat_attachment")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChatAttachment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "conversation", "sender", "parentMessage", "reactions", "attachments" }, allowSetters = true)
    private ChatMessage message;

    @Size(max = 255)
    @Column(name = "file_name", length = 255)
    private String fileName;

    @Size(max = 100)
    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Size(max = 500)
    @Column(name = "file_path", length = 500)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "authorities" }, allowSetters = true)
    private User uploadedBy;

    @NotNull
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    public Long getId() {
        return this.id;
    }

    public ChatAttachment id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatMessage getMessage() {
        return this.message;
    }

    public ChatAttachment message(ChatMessage chatMessage) {
        this.setMessage(chatMessage);
        return this;
    }

    public void setMessage(ChatMessage chatMessage) {
        this.message = chatMessage;
    }

    public String getFileName() {
        return this.fileName;
    }

    public ChatAttachment fileName(String fileName) {
        this.setFileName(fileName);
        return this;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return this.fileType;
    }

    public ChatAttachment fileType(String fileType) {
        this.setFileType(fileType);
        return this;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public ChatAttachment fileSize(Long fileSize) {
        this.setFileSize(fileSize);
        return this;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public ChatAttachment filePath(String filePath) {
        this.setFilePath(filePath);
        return this;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public User getUploadedBy() {
        return this.uploadedBy;
    }

    public ChatAttachment uploadedBy(User user) {
        this.setUploadedBy(user);
        return this;
    }

    public void setUploadedBy(User user) {
        this.uploadedBy = user;
    }

    public Instant getUploadedAt() {
        return this.uploadedAt;
    }

    public ChatAttachment uploadedAt(Instant uploadedAt) {
        this.setUploadedAt(uploadedAt);
        return this;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatAttachment)) {
            return false;
        }
        return getId() != null && getId().equals(((ChatAttachment) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ChatAttachment{" + "id=" + getId() + ", fileName='" + getFileName() + "'" + "}";
    }
}
