package com.gestiontaches.repository;

import com.gestiontaches.domain.ConversationMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link ConversationMember} entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Long> {
    Optional<ConversationMember> findByConversationIdAndUserId(Long conversationId, Long userId);

    List<ConversationMember> findByConversationId(Long conversationId);

    @Modifying
    @Query(
        "DELETE FROM ConversationMember cm WHERE cm.conversation.id IN (SELECT c.id FROM Conversation c WHERE c.project.id = :projectId)"
    )
    int deleteByProjectId(@Param("projectId") Long projectId);

    List<ConversationMember> findByUserId(Long userId);

    @Query(
        "SELECT cm FROM ConversationMember cm LEFT JOIN FETCH cm.user WHERE cm.conversation.id = :conversationId ORDER BY cm.joinedAt ASC"
    )
    List<ConversationMember> findAllByConversationIdFetchUser(@Param("conversationId") Long conversationId);

    @Query("SELECT cm.user.id FROM ConversationMember cm WHERE cm.conversation.id = :conversationId")
    List<Long> findUserIdsByConversationId(@Param("conversationId") Long conversationId);

    @Modifying
    @Query("DELETE FROM ConversationMember cm WHERE cm.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(cm) FROM ConversationMember cm WHERE cm.conversation.id = :conversationId")
    long countMembers(@Param("conversationId") Long conversationId);
}
