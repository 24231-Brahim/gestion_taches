package com.gestiontaches.service;

import com.gestiontaches.domain.Comment;
import com.gestiontaches.domain.Issue;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.CommentRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.CommentDTO;
import com.gestiontaches.service.mapper.CommentMapper;
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

    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.userRepository = userRepository;
    }

    /**
     * Save a comment.
     *
     * @param commentDTO the entity to save.
     * @return the persisted entity.
     */
    public CommentDTO save(CommentDTO commentDTO) {
        LOG.debug("Request to save Comment : {}", commentDTO);
        Comment comment = commentMapper.toEntity(commentDTO);
        comment.setAuthor(getCurrentUser());
        comment = commentRepository.save(comment);
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
                Issue issue = existingComment.getIssue();
                commentMapper.partialUpdate(existingComment, commentDTO);
                preserveDeveloperOwnedFields(existingComment, author, issue);
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
                Issue issue = existingComment.getIssue();
                commentMapper.partialUpdate(existingComment, commentDTO);
                preserveDeveloperOwnedFields(existingComment, author, issue);

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
    public List<CommentDTO> findByIssueId(Long issueId) {
        LOG.debug("Request to get Comments for Issue : {}", issueId);
        return commentRepository.findByIssueIdOrderByCreatedAtDesc(issueId).stream().map(commentMapper::toDto).toList();
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

    private void preserveDeveloperOwnedFields(Comment comment, User author, Issue issue) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.DEVELOPER)) {
            comment.setAuthor(author);
            comment.setIssue(issue);
        }
    }

    private User getCurrentUser() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        return userRepository.findOneByLogin(login).orElseThrow(() -> new RuntimeException("User not found: " + login));
    }
}
