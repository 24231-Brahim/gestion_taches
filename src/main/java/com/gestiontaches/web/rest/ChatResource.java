package com.gestiontaches.web.rest;

import com.gestiontaches.domain.User;
import com.gestiontaches.security.SecurityUtils;
import com.gestiontaches.service.ChatService;
import com.gestiontaches.service.UserService;
import com.gestiontaches.service.dto.ChatMemberDTO;
import com.gestiontaches.service.dto.ChatMessageDTO;
import com.gestiontaches.service.dto.ConversationDTO;
import com.gestiontaches.service.dto.UserPresenceDTO;
import com.gestiontaches.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller managing the project chat.
 *
 * <p>All endpoints live under {@code /api/projects/{projectId}/chat} and require an
 * authenticated user who is a member of the project. Private conversations are only
 * reachable by their participants (enforced in {@link ChatService}).</p>
 */
@RestController
@RequestMapping("/api/projects/{projectId}/chat")
public class ChatResource {

    private static final Logger LOG = LoggerFactory.getLogger(ChatResource.class);

    private static final String ENTITY_NAME = "chat";

    @Value("${jhipster.clientApp.name:gestionTaches}")
    private String applicationName;

    private final ChatService chatService;
    private final UserService userService;

    public ChatResource(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    /**
     * {@code GET /api/projects/{projectId}/chat/conversations} : lists the conversations of
     * the current user in the project (# General first, then the direct conversations).
     */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ConversationDTO>> getConversations(@PathVariable("projectId") Long projectId) {
        LOG.debug("REST request to get chat conversations for project : {}", projectId);
        User currentUser = currentUser();
        return ResponseEntity.ok(chatService.getConversations(projectId, currentUser.getId()));
    }

    /**
     * {@code POST /api/projects/{projectId}/chat/conversations/direct/{userId}} : returns the
     * existing direct conversation with the given member, or creates it.
     */
    @PostMapping("/conversations/direct/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConversationDTO> openDirectConversation(
        @PathVariable("projectId") Long projectId,
        @PathVariable("userId") Long userId
    ) throws URISyntaxException {
        LOG.debug("REST request to open direct conversation in project {} with user {}", projectId, userId);
        User currentUser = currentUser();
        ConversationDTO dto = chatService.getOrCreateDirectConversation(projectId, currentUser.getId(), userId);
        return ResponseEntity.created(new URI("/api/projects/" + projectId + "/chat/conversations/" + dto.getId())).body(dto);
    }

    /**
     * {@code GET /api/projects/{projectId}/chat/conversations/{conversationId}/messages} :
     * returns a page of messages (oldest first). Pass {@code beforeId} to load the previous
     * page (infinite scroll), {@code limit} defaults to 30.
     */
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(
        @PathVariable("projectId") Long projectId,
        @PathVariable("conversationId") Long conversationId,
        @RequestParam(value = "beforeId", required = false) Long beforeId,
        @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        LOG.debug("REST request to get messages of conversation {} in project {}", conversationId, projectId);
        User currentUser = currentUser();
        return ResponseEntity.ok(chatService.getMessages(projectId, conversationId, beforeId, limit, currentUser.getId()));
    }

    /**
     * {@code POST /api/projects/{projectId}/chat/conversations/{conversationId}/messages} :
     * sends a new message.
     */
    @PostMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageDTO> sendMessage(
        @PathVariable("projectId") Long projectId,
        @PathVariable("conversationId") Long conversationId,
        @Valid @RequestBody ChatMessageDTO chatMessageDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to send message in conversation {} of project {}", conversationId, projectId);
        if (chatMessageDTO.getId() != null) {
            throw new BadRequestAlertException("A new message cannot already have an ID", ENTITY_NAME, "idexists");
        }
        User currentUser = currentUser();
        ChatMessageDTO result = chatService.sendMessage(projectId, conversationId, currentUser, chatMessageDTO.getContent());
        return ResponseEntity.created(
            new URI("/api/projects/" + projectId + "/chat/conversations/" + conversationId + "/messages/" + result.getId())
        ).body(result);
    }

    /**
     * {@code PATCH /api/projects/{projectId}/chat/messages/{messageId}} : edits the content of
     * one of the current user's messages.
     */
    @PatchMapping("/messages/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatMessageDTO> updateMessage(
        @PathVariable("projectId") Long projectId,
        @PathVariable("messageId") Long messageId,
        @Valid @RequestBody ChatMessageDTO chatMessageDTO
    ) {
        LOG.debug("REST request to update message {} in project {}", messageId, projectId);
        User currentUser = currentUser();
        return ResponseEntity.ok(chatService.updateMessage(projectId, messageId, currentUser.getId(), chatMessageDTO.getContent()));
    }

    /**
     * {@code DELETE /api/projects/{projectId}/chat/messages/{messageId}} : soft-deletes one of
     * the current user's messages.
     */
    @DeleteMapping("/messages/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMessage(@PathVariable("projectId") Long projectId, @PathVariable("messageId") Long messageId) {
        LOG.debug("REST request to delete message {} in project {}", messageId, projectId);
        User currentUser = currentUser();
        chatService.deleteMessage(projectId, messageId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * {@code POST /api/projects/{projectId}/chat/conversations/{conversationId}/read} : marks
     * the conversation as read for the current user.
     */
    @PostMapping("/conversations/{conversationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markConversationRead(
        @PathVariable("projectId") Long projectId,
        @PathVariable("conversationId") Long conversationId
    ) {
        LOG.debug("REST request to mark conversation {} as read in project {}", conversationId, projectId);
        User currentUser = currentUser();
        chatService.markConversationRead(projectId, conversationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * {@code GET /api/projects/{projectId}/chat/members} : lists the project members with
     * their role and presence (online / last activity).
     */
    @GetMapping("/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMemberDTO>> getMembers(@PathVariable("projectId") Long projectId) {
        LOG.debug("REST request to get chat members of project : {}", projectId);
        User currentUser = currentUser();
        return ResponseEntity.ok(chatService.getMembers(projectId, currentUser.getId()));
    }

    /**
     * {@code GET /api/projects/{projectId}/chat/search} : searches messages across the
     * conversations the current user can access in the project.
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageDTO>> searchMessages(
        @PathVariable("projectId") Long projectId,
        @RequestParam("q") String query,
        @RequestParam(value = "limit", defaultValue = "30") int limit
    ) {
        LOG.debug("REST request to search chat messages in project : {}", projectId);
        User currentUser = currentUser();
        return ResponseEntity.ok(chatService.searchMessages(projectId, currentUser.getId(), query, limit));
    }

    /**
     * {@code POST /api/projects/{projectId}/chat/presence} : heartbeat, refreshes the current
     * user's last activity so the presence indicator stays accurate.
     */
    @PostMapping("/presence")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserPresenceDTO> updatePresence(@PathVariable("projectId") Long projectId) {
        LOG.debug("REST request to refresh chat presence in project : {}", projectId);
        User currentUser = currentUser();
        return ResponseEntity.ok(chatService.updatePresence(projectId, currentUser.getId()));
    }

    /**
     * {@code GET /api/projects/{projectId}/chat/presence} : returns the presence of the
     * project members.
     */
    @GetMapping("/presence")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserPresenceDTO>> getPresence(@PathVariable("projectId") Long projectId) {
        LOG.debug("REST request to get chat presence in project : {}", projectId);
        User currentUser = currentUser();
        return ResponseEntity.ok(chatService.getPresence(projectId, currentUser.getId()));
    }

    private User currentUser() {
        return userService
            .getUserWithAuthoritiesByLogin(
                SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
                    new BadRequestAlertException("User not found", ENTITY_NAME, "usernotfound")
                )
            )
            .orElseThrow(() -> new BadRequestAlertException("User not found", ENTITY_NAME, "usernotfound"));
    }
}
