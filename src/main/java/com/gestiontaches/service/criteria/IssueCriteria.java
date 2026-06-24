package com.gestiontaches.service.criteria;

import com.gestiontaches.domain.enumeration.IssueStatus;
import com.gestiontaches.domain.enumeration.IssueType;
import com.gestiontaches.domain.enumeration.Priority;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.gestiontaches.domain.Issue} entity. This class is used
 * in {@link com.gestiontaches.web.rest.IssueResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /issues?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class IssueCriteria implements Serializable, Criteria {

    /**
     * Class for filtering IssueType
     */
    public static class IssueTypeFilter extends Filter<IssueType> {

        public IssueTypeFilter() {}

        public IssueTypeFilter(IssueTypeFilter filter) {
            super(filter);
        }

        @Override
        public IssueTypeFilter copy() {
            return new IssueTypeFilter(this);
        }
    }

    /**
     * Class for filtering IssueStatus
     */
    public static class IssueStatusFilter extends Filter<IssueStatus> {

        public IssueStatusFilter() {}

        public IssueStatusFilter(IssueStatusFilter filter) {
            super(filter);
        }

        @Override
        public IssueStatusFilter copy() {
            return new IssueStatusFilter(this);
        }
    }

    /**
     * Class for filtering Priority
     */
    public static class PriorityFilter extends Filter<Priority> {

        public PriorityFilter() {}

        public PriorityFilter(PriorityFilter filter) {
            super(filter);
        }

        @Override
        public PriorityFilter copy() {
            return new PriorityFilter(this);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter title;

    private StringFilter description;

    private IssueTypeFilter type;

    private IssueStatusFilter status;

    private PriorityFilter priority;

    private InstantFilter createdAt;

    private InstantFilter updatedAt;

    private LongFilter commentsId;

    private LongFilter attachmentsId;

    private LongFilter historyId;

    private LongFilter sprintId;

    private LongFilter epicId;

    private LongFilter projectId;

    private Boolean distinct;

    public IssueCriteria() {}

    public IssueCriteria(IssueCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.title = other.optionalTitle().map(StringFilter::copy).orElse(null);
        this.description = other.optionalDescription().map(StringFilter::copy).orElse(null);
        this.type = other.optionalType().map(IssueTypeFilter::copy).orElse(null);
        this.status = other.optionalStatus().map(IssueStatusFilter::copy).orElse(null);
        this.priority = other.optionalPriority().map(PriorityFilter::copy).orElse(null);
        this.createdAt = other.optionalCreatedAt().map(InstantFilter::copy).orElse(null);
        this.updatedAt = other.optionalUpdatedAt().map(InstantFilter::copy).orElse(null);
        this.commentsId = other.optionalCommentsId().map(LongFilter::copy).orElse(null);
        this.attachmentsId = other.optionalAttachmentsId().map(LongFilter::copy).orElse(null);
        this.historyId = other.optionalHistoryId().map(LongFilter::copy).orElse(null);
        this.sprintId = other.optionalSprintId().map(LongFilter::copy).orElse(null);
        this.epicId = other.optionalEpicId().map(LongFilter::copy).orElse(null);
        this.projectId = other.optionalProjectId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public IssueCriteria copy() {
        return new IssueCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getTitle() {
        return title;
    }

    public Optional<StringFilter> optionalTitle() {
        return Optional.ofNullable(title);
    }

    public StringFilter title() {
        if (title == null) {
            setTitle(new StringFilter());
        }
        return title;
    }

    public void setTitle(StringFilter title) {
        this.title = title;
    }

    public StringFilter getDescription() {
        return description;
    }

    public Optional<StringFilter> optionalDescription() {
        return Optional.ofNullable(description);
    }

    public StringFilter description() {
        if (description == null) {
            setDescription(new StringFilter());
        }
        return description;
    }

    public void setDescription(StringFilter description) {
        this.description = description;
    }

    public IssueTypeFilter getType() {
        return type;
    }

    public Optional<IssueTypeFilter> optionalType() {
        return Optional.ofNullable(type);
    }

    public IssueTypeFilter type() {
        if (type == null) {
            setType(new IssueTypeFilter());
        }
        return type;
    }

    public void setType(IssueTypeFilter type) {
        this.type = type;
    }

    public IssueStatusFilter getStatus() {
        return status;
    }

    public Optional<IssueStatusFilter> optionalStatus() {
        return Optional.ofNullable(status);
    }

    public IssueStatusFilter status() {
        if (status == null) {
            setStatus(new IssueStatusFilter());
        }
        return status;
    }

    public void setStatus(IssueStatusFilter status) {
        this.status = status;
    }

    public PriorityFilter getPriority() {
        return priority;
    }

    public Optional<PriorityFilter> optionalPriority() {
        return Optional.ofNullable(priority);
    }

    public PriorityFilter priority() {
        if (priority == null) {
            setPriority(new PriorityFilter());
        }
        return priority;
    }

    public void setPriority(PriorityFilter priority) {
        this.priority = priority;
    }

    public InstantFilter getCreatedAt() {
        return createdAt;
    }

    public Optional<InstantFilter> optionalCreatedAt() {
        return Optional.ofNullable(createdAt);
    }

    public InstantFilter createdAt() {
        if (createdAt == null) {
            setCreatedAt(new InstantFilter());
        }
        return createdAt;
    }

    public void setCreatedAt(InstantFilter createdAt) {
        this.createdAt = createdAt;
    }

    public InstantFilter getUpdatedAt() {
        return updatedAt;
    }

    public Optional<InstantFilter> optionalUpdatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    public InstantFilter updatedAt() {
        if (updatedAt == null) {
            setUpdatedAt(new InstantFilter());
        }
        return updatedAt;
    }

    public void setUpdatedAt(InstantFilter updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LongFilter getCommentsId() {
        return commentsId;
    }

    public Optional<LongFilter> optionalCommentsId() {
        return Optional.ofNullable(commentsId);
    }

    public LongFilter commentsId() {
        if (commentsId == null) {
            setCommentsId(new LongFilter());
        }
        return commentsId;
    }

    public void setCommentsId(LongFilter commentsId) {
        this.commentsId = commentsId;
    }

    public LongFilter getAttachmentsId() {
        return attachmentsId;
    }

    public Optional<LongFilter> optionalAttachmentsId() {
        return Optional.ofNullable(attachmentsId);
    }

    public LongFilter attachmentsId() {
        if (attachmentsId == null) {
            setAttachmentsId(new LongFilter());
        }
        return attachmentsId;
    }

    public void setAttachmentsId(LongFilter attachmentsId) {
        this.attachmentsId = attachmentsId;
    }

    public LongFilter getHistoryId() {
        return historyId;
    }

    public Optional<LongFilter> optionalHistoryId() {
        return Optional.ofNullable(historyId);
    }

    public LongFilter historyId() {
        if (historyId == null) {
            setHistoryId(new LongFilter());
        }
        return historyId;
    }

    public void setHistoryId(LongFilter historyId) {
        this.historyId = historyId;
    }

    public LongFilter getSprintId() {
        return sprintId;
    }

    public Optional<LongFilter> optionalSprintId() {
        return Optional.ofNullable(sprintId);
    }

    public LongFilter sprintId() {
        if (sprintId == null) {
            setSprintId(new LongFilter());
        }
        return sprintId;
    }

    public void setSprintId(LongFilter sprintId) {
        this.sprintId = sprintId;
    }

    public LongFilter getEpicId() {
        return epicId;
    }

    public Optional<LongFilter> optionalEpicId() {
        return Optional.ofNullable(epicId);
    }

    public LongFilter epicId() {
        if (epicId == null) {
            setEpicId(new LongFilter());
        }
        return epicId;
    }

    public void setEpicId(LongFilter epicId) {
        this.epicId = epicId;
    }

    public LongFilter getProjectId() {
        return projectId;
    }

    public Optional<LongFilter> optionalProjectId() {
        return Optional.ofNullable(projectId);
    }

    public LongFilter projectId() {
        if (projectId == null) {
            setProjectId(new LongFilter());
        }
        return projectId;
    }

    public void setProjectId(LongFilter projectId) {
        this.projectId = projectId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final IssueCriteria that = (IssueCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(title, that.title) &&
            Objects.equals(description, that.description) &&
            Objects.equals(type, that.type) &&
            Objects.equals(status, that.status) &&
            Objects.equals(priority, that.priority) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(commentsId, that.commentsId) &&
            Objects.equals(attachmentsId, that.attachmentsId) &&
            Objects.equals(historyId, that.historyId) &&
            Objects.equals(sprintId, that.sprintId) &&
            Objects.equals(epicId, that.epicId) &&
            Objects.equals(projectId, that.projectId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            title,
            description,
            type,
            status,
            priority,
            createdAt,
            updatedAt,
            commentsId,
            attachmentsId,
            historyId,
            sprintId,
            epicId,
            projectId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "IssueCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalTitle().map(f -> "title=" + f + ", ").orElse("") +
            optionalDescription().map(f -> "description=" + f + ", ").orElse("") +
            optionalType().map(f -> "type=" + f + ", ").orElse("") +
            optionalStatus().map(f -> "status=" + f + ", ").orElse("") +
            optionalPriority().map(f -> "priority=" + f + ", ").orElse("") +
            optionalCreatedAt().map(f -> "createdAt=" + f + ", ").orElse("") +
            optionalUpdatedAt().map(f -> "updatedAt=" + f + ", ").orElse("") +
            optionalCommentsId().map(f -> "commentsId=" + f + ", ").orElse("") +
            optionalAttachmentsId().map(f -> "attachmentsId=" + f + ", ").orElse("") +
            optionalHistoryId().map(f -> "historyId=" + f + ", ").orElse("") +
            optionalSprintId().map(f -> "sprintId=" + f + ", ").orElse("") +
            optionalEpicId().map(f -> "epicId=" + f + ", ").orElse("") +
            optionalProjectId().map(f -> "projectId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
