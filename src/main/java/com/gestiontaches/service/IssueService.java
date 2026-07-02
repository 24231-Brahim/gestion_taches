package com.gestiontaches.service;

import com.gestiontaches.domain.Issue;
import com.gestiontaches.domain.User;
import com.gestiontaches.repository.IssueRepository;
import com.gestiontaches.service.dto.IssueDTO;
import com.gestiontaches.service.mapper.IssueMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.gestiontaches.domain.Issue}.
 */
@Service
@Transactional
public class IssueService {

    private static final Logger LOG = LoggerFactory.getLogger(IssueService.class);

    private final IssueRepository issueRepository;

    private final IssueMapper issueMapper;

    public IssueService(IssueRepository issueRepository, IssueMapper issueMapper) {
        this.issueRepository = issueRepository;
        this.issueMapper = issueMapper;
    }

    /**
     * Save a issue.
     *
     * @param issueDTO the entity to save.
     * @return the persisted entity.
     */
    public IssueDTO save(IssueDTO issueDTO) {
        LOG.debug("Request to save Issue : {}", issueDTO);
        Issue issue = issueMapper.toEntity(issueDTO);
        issue = issueRepository.save(issue);
        return issueMapper.toDto(issue);
    }

    /**
     * Update a issue.
     *
     * @param issueDTO the entity to save.
     * @return the persisted entity.
     */
    public IssueDTO update(IssueDTO issueDTO) {
        LOG.debug("Request to update Issue : {}", issueDTO);
        Issue issue = issueMapper.toEntity(issueDTO);
        issue = issueRepository.save(issue);
        return issueMapper.toDto(issue);
    }

    /**
     * Partially update a issue.
     *
     * @param issueDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<IssueDTO> partialUpdate(IssueDTO issueDTO) {
        LOG.debug("Request to partially update Issue : {}", issueDTO);

        return issueRepository
            .findById(issueDTO.getId())
            .map(existingIssue -> {
                issueMapper.partialUpdate(existingIssue, issueDTO);

                return existingIssue;
            })
            .map(issueRepository::save)
            .map(issueMapper::toDto);
    }

    /**
     * Get all the issues with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<IssueDTO> findAllWithEagerRelationships(Pageable pageable) {
        return issueRepository.findAllWithEagerRelationships(pageable).map(issueMapper::toDto);
    }

    /**
     * Get one issue by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<IssueDTO> findOne(Long id) {
        LOG.debug("Request to get Issue : {}", id);
        return issueRepository.findOneWithEagerRelationships(id).map(issueMapper::toDto);
    }

    /**
     * Delete the issue by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Issue : {}", id);
        issueRepository.deleteById(id);
    }

    /**
     * Assign a user to an issue.
     *
     * @param issueId the id of the issue.
     * @param user the user to assign.
     * @return the updated issue DTO.
     */
    public IssueDTO assign(Long issueId, User user) {
        LOG.debug("Request to assign user {} to Issue : {}", user.getLogin(), issueId);
        return issueRepository
            .findById(issueId)
            .map(issue -> {
                issue.setAssignee(user);
                return issueRepository.save(issue);
            })
            .map(issueMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Issue not found with id " + issueId));
    }
}
