package com.gestiontaches.service;

import com.gestiontaches.domain.Attachment;
import com.gestiontaches.domain.Task;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.AttachmentRepository;
import com.gestiontaches.repository.TaskRepository;
import com.gestiontaches.repository.UserRepository;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.AttachmentDTO;
import com.gestiontaches.service.mapper.AttachmentMapper;
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
 * Service Implementation for managing {@link com.gestiontaches.domain.Attachment}.
 */
@Service
@Transactional
public class AttachmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository attachmentRepository;

    private final AttachmentMapper attachmentMapper;

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    private final ProjectPermissionService projectPermissionService;

    public AttachmentService(
        AttachmentRepository attachmentRepository,
        AttachmentMapper attachmentMapper,
        UserRepository userRepository,
        TaskRepository taskRepository,
        ProjectPermissionService projectPermissionService
    ) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentMapper = attachmentMapper;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.projectPermissionService = projectPermissionService;
    }

    /**
     * Save a attachment.
     *
     * @param attachmentDTO the entity to save.
     * @return the persisted entity.
     */
    public AttachmentDTO save(AttachmentDTO attachmentDTO) {
        LOG.debug("Request to save Attachment : {}", attachmentDTO);
        Task task = taskRepository.findById(attachmentDTO.getTask().getId()).orElseThrow(() -> new RuntimeException("Task not found"));
        // Same rule as comments: uploading requires at least view access to the task's project
        // (membership, or ADMIN/PROJET_MANAGER) — not just any authenticated account.
        projectPermissionService.requireProjectAccess(task.getProject().getId());
        Attachment attachment = attachmentMapper.toEntity(attachmentDTO);
        attachment.setTask(task);
        attachment.setUploadedBy(getCurrentUser());
        attachment = attachmentRepository.save(attachment);
        return attachmentMapper.toDto(attachment);
    }

    /**
     * Update a attachment.
     *
     * @param attachmentDTO the entity to save.
     * @return the persisted entity.
     */
    public AttachmentDTO update(AttachmentDTO attachmentDTO) {
        LOG.debug("Request to update Attachment : {}", attachmentDTO);
        return attachmentRepository
            .findById(attachmentDTO.getId())
            .map(existingAttachment -> {
                checkCanModifyAttachment(existingAttachment);
                User uploadedBy = existingAttachment.getUploadedBy();
                Task task = existingAttachment.getTask();
                attachmentMapper.partialUpdate(existingAttachment, attachmentDTO);
                preserveOwnedFields(existingAttachment, uploadedBy, task);
                return existingAttachment;
            })
            .map(attachmentRepository::save)
            .map(attachmentMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }

    /**
     * Partially update a attachment.
     *
     * @param attachmentDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<AttachmentDTO> partialUpdate(AttachmentDTO attachmentDTO) {
        LOG.debug("Request to partially update Attachment : {}", attachmentDTO);

        return attachmentRepository
            .findById(attachmentDTO.getId())
            .map(existingAttachment -> {
                checkCanModifyAttachment(existingAttachment);
                User uploadedBy = existingAttachment.getUploadedBy();
                Task task = existingAttachment.getTask();
                attachmentMapper.partialUpdate(existingAttachment, attachmentDTO);
                preserveOwnedFields(existingAttachment, uploadedBy, task);

                return existingAttachment;
            })
            .map(attachmentRepository::save)
            .map(attachmentMapper::toDto);
    }

    /**
     * Get all the attachments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<AttachmentDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Attachments");
        return attachmentRepository.findAll(pageable).map(attachmentMapper::toDto);
    }

    /**
     * Get one attachment by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<AttachmentDTO> findOne(Long id) {
        LOG.debug("Request to get Attachment : {}", id);
        return attachmentRepository.findById(id).map(attachmentMapper::toDto);
    }

    /**
     * Delete the attachment by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Attachment : {}", id);
        Attachment attachment = attachmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Attachment not found"));
        checkCanModifyAttachment(attachment);
        attachmentRepository.delete(attachment);
    }

    @Transactional(readOnly = true)
    public List<AttachmentDTO> findByTaskId(Long taskId) {
        LOG.debug("Request to get Attachments for Task : {}", taskId);
        return attachmentRepository.findByTaskIdOrderByUploadedAtDesc(taskId).stream().map(attachmentMapper::toDto).toList();
    }

    private void checkCanModifyAttachment(Attachment attachment) {
        if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN, AuthoritiesConstants.PROJET_MANAGER)) {
            return;
        }

        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        User uploadedBy = attachment.getUploadedBy();
        if (uploadedBy == null || !login.equals(uploadedBy.getLogin())) {
            throw new AccessDeniedException("User can only modify own attachments");
        }
    }

    private void preserveOwnedFields(Attachment attachment, User uploadedBy, Task task) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.DEVELOPER)) {
            attachment.setUploadedBy(uploadedBy);
            attachment.setTask(task);
        }
    }

    private User getCurrentUser() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user not found"));
        return userRepository.findOneByLogin(login).orElseThrow(() -> new RuntimeException("User not found: " + login));
    }
}
