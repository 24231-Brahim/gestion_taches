package com.gestiontaches.service;

import com.gestiontaches.domain.*;
import com.gestiontaches.domain.enumeration.ConversationType;
import com.gestiontaches.repository.*;
import com.gestiontaches.security.AuthoritiesConstants;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.dto.ChatMemberDTO;
import com.gestiontaches.service.dto.ChatMessageDTO;
import com.gestiontaches.service.dto.ConversationDTO;
import com.gestiontaches.service.mapper.ChatMessageMapper;
import com.gestiontaches.service.mapper.ConversationMapper;
import com.gestiontaches.web.rest.errors.BadRequestAlertException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatService {

    private static final Logger LOG = LoggerFactory.getLogger(ChatService.class);

    private static final String ENTITY_NAME = "chat";

    private static final int PREVIEW_LENGTH = 120;

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;

    public ChatService(
        ConversationRepository conversationRepository,
        ConversationMemberRepository conversationMemberRepository,
        ChatMessageRepository chatMessageRepository,
        ProjectMemberRepository projectMemberRepository,
        ProjectRepository projectRepository,
        ConversationMapper conversationMapper,
        ChatMessageMapper chatMessageMapper
    ) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.conversationMapper = conversationMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    @Transactional
    public List<ConversationDTO> getConversations(Long projectId, Long currentUserId) {
        requireProjectMember(projectId, currentUserId);
        Conversation general = ensureGeneralConversation(projectId, currentUserId);
        List<Conversation> visible = conversationRepository.findVisibleForUser(projectId, currentUserId, ConversationType.DIRECT);

        List<ConversationDTO> result = new ArrayList<>();
        result.add(toConversationDTO(general, currentUserId));
        for (Conversation conversation : visible) {
            result.add(toConversationDTO(conversation, currentUserId));
        }
        return result;
    }

    @Transactional
    public Long getGeneralConversationId(Long projectId, Long currentUserId) {
        requireProjectMember(projectId, currentUserId);
        return ensureGeneralConversation(projectId, currentUserId).getId();
    }

    @Transactional
    public ConversationDTO getOrCreateDirectConversation(Long projectId, Long currentUserId, Long otherUserId) {
        requireProjectMember(projectId, currentUserId);
        requireProjectMember(projectId, otherUserId);
        if (currentUserId.equals(otherUserId)) {
            throw new BadRequestAlertException("Cannot open a direct conversation with yourself", ENTITY_NAME, "selfdirect");
        }
        Conversation conversation = conversationRepository
            .findDirectBetween(projectId, currentUserId, otherUserId)
            .orElseGet(() -> createDirectConversation(projectId, currentUserId, otherUserId));
        return toConversationDTO(conversation, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessages(Long projectId, Long conversationId, Long beforeId, int limit, Long currentUserId) {
        requireConversationAccess(projectId, conversationId, currentUserId);
        int size = Math.clamp(limit, 1, 100);
        List<ChatMessage> page;
        if (beforeId == null) {
            page = chatMessageRepository.findLatest(conversationId, PageRequest.of(0, size));
        } else {
            page = chatMessageRepository.findOlderThan(conversationId, beforeId, PageRequest.of(0, size));
        }
        Collections.reverse(page);
        return page.stream().map(chatMessageMapper::toDto).toList();
    }

    @Transactional
    public ChatMessageDTO sendMessage(Long projectId, Long conversationId, User sender, String content) {
        Conversation conversation = requireConversationAccess(projectId, conversationId, sender.getId());
        ChatMessage message = new ChatMessage();
        message.content(content);
        message.conversation(conversation);
        message.sender(sender);
        message.createdAt(Instant.now());
        message = chatMessageRepository.save(message);

        markRead(conversationId, sender.getId());
        return chatMessageMapper.toDto(message);
    }

    @Transactional
    public ChatMessageDTO updateMessage(Long projectId, Long messageId, Long currentUserId, String content) {
        ChatMessage message = requireMessageAccess(projectId, messageId, currentUserId);
        message.content(content);
        message.editedAt(Instant.now());
        return chatMessageMapper.toDto(chatMessageRepository.save(message));
    }

    @Transactional
    public void deleteMessage(Long projectId, Long messageId, Long currentUserId) {
        ChatMessage message = requireMessageAccess(projectId, messageId, currentUserId);
        message.deleted(true);
        message.content("message deleted");
        chatMessageRepository.save(message);
    }

    @Transactional
    public void markConversationRead(Long projectId, Long conversationId, Long currentUserId) {
        requireConversationAccess(projectId, conversationId, currentUserId);
        markRead(conversationId, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<ChatMemberDTO> getMembers(Long projectId, Long currentUserId) {
        requireProjectMember(projectId, currentUserId);
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        return members
            .stream()
            .map(member -> toChatMemberDTO(member, null))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> searchMessages(Long projectId, Long currentUserId, String query, int limit) {
        requireProjectMember(projectId, currentUserId);
        if (query == null || query.isBlank()) {
            return List.of();
        }
        int size = Math.clamp(limit, 1, 100);
        List<ChatMessage> results = chatMessageRepository.searchProject(projectId, currentUserId, query.trim(), PageRequest.of(0, size));
        return results.stream().map(chatMessageMapper::toDto).toList();
    }

    private Conversation ensureGeneralConversation(Long projectId, Long currentUserId) {
        return conversationRepository
            .findByProjectIdAndType(projectId, ConversationType.GENERAL)
            .orElseGet(() -> createGeneralConversation(projectId, currentUserId));
    }

    @Transactional
    protected Conversation createGeneralConversation(Long projectId, Long currentUserId) {
        Project project = new Project();
        project.setId(projectId);
        User creator = userFromId(currentUserId);

        Conversation conversation = new Conversation();
        conversation.type(ConversationType.GENERAL);
        conversation.project(project);
        conversation.createdBy(creator);
        conversation.createdAt(Instant.now());
        conversation = conversationRepository.save(conversation);

        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        Instant now = Instant.now();
        for (ProjectMember member : members) {
            conversation.addMember(conversationMember(member.getUser(), now, null));
        }
        return conversationRepository.save(conversation);
    }

    private Conversation createDirectConversation(Long projectId, Long currentUserId, Long otherUserId) {
        Project project = new Project();
        project.setId(projectId);
        Instant now = Instant.now();

        Conversation conversation = new Conversation();
        conversation.type(ConversationType.DIRECT);
        conversation.project(project);
        conversation.createdBy(userFromId(currentUserId));
        conversation.createdAt(now);
        conversation.addMember(conversationMember(userFromId(currentUserId), now, now));
        conversation.addMember(conversationMember(userFromId(otherUserId), now, null));
        return conversationRepository.save(conversation);
    }

    private ConversationMember conversationMember(User user, Instant joinedAt, Instant lastReadAt) {
        ConversationMember member = new ConversationMember();
        member.user(user);
        member.joinedAt(joinedAt);
        member.lastReadAt(lastReadAt);
        return member;
    }

    private ConversationDTO toConversationDTO(Conversation conversation, Long currentUserId) {
        ConversationDTO dto = conversationMapper.toDto(conversation);

        ChatMessage last = chatMessageRepository.findLatest(conversation.getId(), PageRequest.of(0, 1)).stream().findFirst().orElse(null);
        if (last != null) {
            dto.setLastMessageAt(last.getCreatedAt());
            dto.setLastMessagePreview(truncatePreview(last.getContent()));
        }

        long unread = conversationMemberRepository
            .findByConversationIdAndUserId(conversation.getId(), currentUserId)
            .map(ConversationMember::getLastReadAt)
            .map(lastRead -> chatMessageRepository.countSince(conversation.getId(), lastRead))
            .orElseGet(() -> chatMessageRepository.countByConversationId(conversation.getId()));
        dto.setUnreadCount(unread);

        List<ChatMemberDTO> participants = conversationMemberRepository
            .findAllByConversationIdFetchUser(conversation.getId())
            .stream()
            .map(member -> toChatMemberDTOFromConversationMember(member))
            .toList();
        dto.setParticipants(participants);
        return dto;
    }

    private ChatMemberDTO toChatMemberDTO(ProjectMember member, ConversationMember conversationMember) {
        ChatMemberDTO dto = new ChatMemberDTO();
        dto.setUserId(member.getUser().getId());
        dto.setUserLogin(member.getUser().getLogin());
        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());
        if (conversationMember != null) {
            dto.setLastReadAt(conversationMember.getLastReadAt());
        }
        return dto;
    }

    private ChatMemberDTO toChatMemberDTOFromConversationMember(ConversationMember member) {
        ChatMemberDTO dto = new ChatMemberDTO();
        dto.setUserId(member.getUser().getId());
        dto.setUserLogin(member.getUser().getLogin());
        dto.setJoinedAt(member.getJoinedAt());
        dto.setLastReadAt(member.getLastReadAt());
        return dto;
    }

    private String truncatePreview(String content) {
        if (content == null) {
            return null;
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() <= PREVIEW_LENGTH ? normalized : normalized.substring(0, PREVIEW_LENGTH - 1) + "…";
    }

    private void markRead(Long conversationId, Long userId) {
        conversationMemberRepository.findByConversationIdAndUserId(conversationId, userId).ifPresent(member -> {
            member.setLastReadAt(Instant.now());
            conversationMemberRepository.save(member);
        });
    }

    private User userFromId(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private void requireProjectMember(Long projectId, Long userId) {
        if (!projectRepository.existsById(projectId)) {
            throw new BadRequestAlertException("Project not found", ENTITY_NAME, "projectnotfound");
        }
        if (SecurityUtils.hasCurrentUserAnyOfAuthorities(AuthoritiesConstants.ADMIN, AuthoritiesConstants.PROJET_MANAGER)) {
            return;
        }
        if (projectMemberRepository.findByProjectIdAndUserId(projectId, userId).isEmpty()) {
            throw new BadRequestAlertException("You are not a member of this project", ENTITY_NAME, "notprojectmember");
        }
    }

    private Conversation requireConversationAccess(Long projectId, Long conversationId, Long userId) {
        Conversation conversation = conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new BadRequestAlertException("Conversation not found", ENTITY_NAME, "conversationnotfound"));
        if (!Objects.equals(conversation.getProject().getId(), projectId)) {
            throw new BadRequestAlertException("Conversation does not belong to this project", ENTITY_NAME, "conversationnotfound");
        }
        boolean isGeneral = conversation.getType() == ConversationType.GENERAL;
        boolean isMember = conversationMemberRepository.findByConversationIdAndUserId(conversationId, userId).isPresent();
        boolean isGlobalAdmin = SecurityUtils.hasCurrentUserAnyOfAuthorities(
            AuthoritiesConstants.ADMIN,
            AuthoritiesConstants.PROJET_MANAGER
        );
        if (!isGeneral && !isMember && !isGlobalAdmin) {
            throw new BadRequestAlertException("You are not part of this conversation", ENTITY_NAME, "notconversationmember");
        }
        if (isGeneral) {
            requireProjectMember(projectId, userId);
        }
        return conversation;
    }

    private ChatMessage requireMessageAccess(Long projectId, Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository
            .findById(messageId)
            .orElseThrow(() -> new BadRequestAlertException("Message not found", ENTITY_NAME, "messagenotfound"));
        requireConversationAccess(projectId, message.getConversation().getId(), userId);
        if (!Objects.equals(message.getSender().getId(), userId)) {
            throw new BadRequestAlertException("You can only edit or delete your own messages", ENTITY_NAME, "notownermessage");
        }
        return message;
    }
}
