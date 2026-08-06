package com.gestiontaches.repository;

import com.gestiontaches.domain.ChatMessage;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link ChatMessage} entity.
 *
 * <p>Messages are fetched with a cursor on the primary key ({@code beforeId}) so the chat
 * never loads the whole history at once (infinite pagination).</p>
 */
@SuppressWarnings("unused")
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query(
        "SELECT m FROM ChatMessage m LEFT JOIN FETCH m.sender WHERE m.conversation.id = :conversationId " +
            "AND m.id < :beforeId ORDER BY m.id DESC"
    )
    List<ChatMessage> findOlderThan(@Param("conversationId") Long conversationId, @Param("beforeId") Long beforeId, Pageable pageable);

    @Query("SELECT m FROM ChatMessage m LEFT JOIN FETCH m.sender WHERE m.conversation.id = :conversationId ORDER BY m.id DESC")
    List<ChatMessage> findLatest(@Param("conversationId") Long conversationId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.createdAt > :after")
    long countSince(@Param("conversationId") Long conversationId, @Param("after") java.time.Instant after);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId")
    long countByConversationId(@Param("conversationId") Long conversationId);

    @Query(
        "SELECT m FROM ChatMessage m LEFT JOIN FETCH m.sender WHERE m.conversation.id = :conversationId " +
            "AND lower(m.content) LIKE lower(concat('%', :query, '%')) AND m.deleted = false ORDER BY m.id DESC"
    )
    List<ChatMessage> search(@Param("conversationId") Long conversationId, @Param("query") String query, Pageable pageable);

    @Query(
        "SELECT m FROM ChatMessage m LEFT JOIN FETCH m.sender WHERE m.conversation.project.id = :projectId AND " +
            "m.conversation.id IN (SELECT cm.conversation.id FROM ConversationMember cm WHERE cm.user.id = :userId) " +
            "AND lower(m.content) LIKE lower(concat('%', :query, '%')) AND m.deleted = false ORDER BY m.id DESC"
    )
    List<ChatMessage> searchProject(
        @Param("projectId") Long projectId,
        @Param("userId") Long userId,
        @Param("query") String query,
        Pageable pageable
    );
}
