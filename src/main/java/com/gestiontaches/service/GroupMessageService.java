package com.gestiontaches.service;

import com.gestiontaches.domain.GroupMessage;
import com.gestiontaches.repository.GroupMessageRepository;
import com.gestiontaches.service.dto.GroupMessageDTO;
import com.gestiontaches.service.mapper.GroupMessageMapper;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.gestiontaches.domain.GroupMessage}.
 */
@Service
@Transactional
public class GroupMessageService {

    private static final Logger LOG = LoggerFactory.getLogger(GroupMessageService.class);

    private final GroupMessageRepository groupMessageRepository;

    private final GroupMessageMapper groupMessageMapper;

    public GroupMessageService(GroupMessageRepository groupMessageRepository, GroupMessageMapper groupMessageMapper) {
        this.groupMessageRepository = groupMessageRepository;
        this.groupMessageMapper = groupMessageMapper;
    }

    /**
     * Save a groupMessage.
     *
     * @param groupMessageDTO the entity to save.
     * @return the persisted entity.
     */
    public GroupMessageDTO save(GroupMessageDTO groupMessageDTO) {
        LOG.debug("Request to save GroupMessage : {}", groupMessageDTO);
        GroupMessage groupMessage = groupMessageMapper.toEntity(groupMessageDTO);
        groupMessage = groupMessageRepository.save(groupMessage);
        return groupMessageMapper.toDto(groupMessage);
    }

    /**
     * Update a groupMessage.
     *
     * @param groupMessageDTO the entity to save.
     * @return the persisted entity.
     */
    public GroupMessageDTO update(GroupMessageDTO groupMessageDTO) {
        LOG.debug("Request to update GroupMessage : {}", groupMessageDTO);
        GroupMessage groupMessage = groupMessageMapper.toEntity(groupMessageDTO);
        groupMessage = groupMessageRepository.save(groupMessage);
        return groupMessageMapper.toDto(groupMessage);
    }

    /**
     * Partially update a groupMessage.
     *
     * @param groupMessageDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<GroupMessageDTO> partialUpdate(GroupMessageDTO groupMessageDTO) {
        LOG.debug("Request to partially update GroupMessage : {}", groupMessageDTO);

        return groupMessageRepository
            .findById(groupMessageDTO.getId())
            .map(existingGroupMessage -> {
                groupMessageMapper.partialUpdate(existingGroupMessage, groupMessageDTO);

                return existingGroupMessage;
            })
            .map(groupMessageRepository::save)
            .map(groupMessageMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<GroupMessageDTO> findOne(Long id) {
        LOG.debug("Request to get GroupMessage : {}", id);
        return groupMessageRepository.findById(id).map(groupMessageMapper::toDto);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete GroupMessage : {}", id);
        groupMessageRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<GroupMessageDTO> findVisibleMessages(Long projectId, Long userId) {
        LOG.debug("Request to get visible GroupMessages for project {} and user {}", projectId, userId);
        return groupMessageRepository.findVisibleMessages(projectId, userId).stream().map(groupMessageMapper::toDto).toList();
    }
}
