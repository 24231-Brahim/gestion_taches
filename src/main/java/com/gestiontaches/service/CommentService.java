package com.gestiontaches.service;

import com.gestiontaches.domain.Comment;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.CommentRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.CommentDTO;
import com.gestiontaches.service.mapper.CommentMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.gestiontaches.domain.Comment}.
 */
@Service
@Transactional
public class CommentService {

    private static final Logger LOG = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;

    private final CommentMapper commentMapper;

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final ProjectPermissionService projectPermissionService;

    private final NotificationService notificationService;

    public CommentService(
        CommentRepository commentRepository,
        CommentMapper commentMapper,
        UserRepository userRepository,
        TaskRepository taskRepository,
        ProjectPermissionService projectPermissionService,
        NotificationService notificationService
    ) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.projectPermissionService = projectPermissionService;
        this.notificationService = notificationService;
    }

    /**
     * Save a comment.
     *
     * @param commentDTO the entity to save.
     * @return the persisted entity.
     */
    public CommentDTO save(CommentDTO commentDTO) {
        LOG.debug("Request to save Comment : {}", commentDTO);
        Task task = taskRepository.findById(commentDTO.getTask().getId()).orElseThrow(() -> new RuntimeException("Task not found"));
        // Anyone commenting must at least be able to view the task: a project member, or
        // ADMIN/PROJET_MANAGER. Prevents an unrelated developer from spamming comments on tasks in
        // projects they don't belong to just by guessing a taskId.
        projectPermissionService.requireProjectAccess(task.getProject().getId());
        Comment comment = commentMapper.toEntity(commentDTO);
        comment.setTask(task);
        User author = getCurrentUser();
        comment.setAuthor(author);
        comment.setCreatedAt(Instant.now());
        comment = commentRepository.save(comment);

        // Notify task assignee and task creator (excluding the comment author)
        User assignee = task.getAssignee();
        User creator = task.getCreatedBy();
        String commentMessage = "Un nouveau commentaire a été ajouté sur la tâche \"" + task.getTitle() + "\"";

        if (assignee != null && !assignee.getId().equals(author.getId())) {
            notificationService.createNotification(assignee, commentMessage, task.getTitle(), task);
        }
        if (creator != null && !creator.getId().equals(author.getId())) {
            if (assignee == null || !assignee.getId().equals(creator.getId())) {
                notificationService.createNotification(creator, commentMessage, task.getTitle(), task);
            }
        }

        return commentMapper.toDto(comment);
    }

    /**
     * Update a comment.
     *
     * @param commentDTO the entity to save.
     * @return the persisted entity.
     */
    public CommentDTO update(CommentDTO commentDTO) {
        LOG.debug("Request to update Comment : {}", commentDTO);
        return commentRepository
            .findById(commentDTO.getId())
            .map(existingComment -> {
                checkCanModifyComment(existingComment);
                User author = existingComment.getAuthor();
                Task task = existingComment.getTask();
                commentMapper.partialUpdate(existingComment, commentDTO);
                preserveDeveloperOwnedFields(existingComment, author, task);
                return existingComment;
            })
            .map(commentRepository::save)
            .map(commentMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    /**
     * Partially update a comment.
     *
     * @param commentDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CommentDTO> partialUpdate(CommentDTO commentDTO) {
        LOG.debug("Request to partially update Comment : {}", commentDTO);

        return commentRepository
            .findById(commentDTO.getId())
            .map(existingComment -> {
                checkCanModifyComment(existingComment);
                User author = existingComment.getAuthor();
                Task task = existingComment.getTask();
                commentMapper.partialUpdate(existingComment, commentDTO);
                preserveDeveloperOwnedFields(existingComment, author, task);

                return existingComment;
            })
            .map(commentRepository::save)
            .map(commentMapper::toDto);
    }

    /**
     * Get all the comments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<CommentDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Comments");
        return commentRepository.findAll(pageable).map(commentMapper::toDto);
    }

    /**
     * Get one comment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CommentDTO> findOne(Long id) {
        LOG.debug("Request to get Comment : {}", id);
        return commentRepository.findById(id).map(commentMapper::toDto);
    }

    /**
     * Delete the comment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Comment : {}", id);
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        checkCanModifyComment(comment);
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findByTaskId(Long taskId) {
        LOG.debug("Request to get Comments for Task : {}", taskId);
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream().map(commentMapper::toDto).toList();
    }

    private void checkCanModifyComment(Comment comment) {
        if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN, AuthoritiesConstants.PROJET_MANAGER)) {
            return;
        }

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        User author = comment.getAuthor();
        if (author == null || !login.equals(author.getLogin())) {
            throw new AccessDeniedException("User can only modify own comments");
        }
    }

    private void preserveDeveloperOwnedFields(Comment comment, User author, Task task) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.DEVELOPER)) {
            comment.setAuthor(author);
            comment.setTask(task);
        }
    }

    private User getCurrentUser() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        return userRepository.findOneByLogin(login).orElseThrow(() -> new RuntimeException("User not found: " + login));
    }
}
