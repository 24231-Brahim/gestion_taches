package com.gestiontaches.repository;

import com.gestiontaches.domain.Conversation;
import com.gestiontaches.domain.enumeration.ConversationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link Conversation} entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByProjectIdAndType(Long projectId, ConversationType type);

    List<Conversation> findByProjectIdOrderByCreatedAtAsc(Long projectId);

    @Modifying
    @Query("DELETE FROM Conversation c WHERE c.project.id = :projectId")
    int deleteByProjectId(@Param("projectId") Long projectId);

    @Query(
        "SELECT c FROM Conversation c WHERE c.project.id = :projectId AND c.type = :type AND EXISTS (" +
            "SELECT m FROM ConversationMember m WHERE m.conversation = c AND m.user.id = :userId" +
            ") ORDER BY c.createdAt ASC"
    )
    List<Conversation> findVisibleForUser(
        @Param("projectId") Long projectId,
        @Param("userId") Long userId,
        @Param("type") ConversationType type
    );

    @Query(
        "SELECT c FROM Conversation c WHERE c.project.id = :projectId AND c.type = 'DIRECT' AND " +
            "(SELECT COUNT(m) FROM ConversationMember m WHERE m.conversation = c) = 2 AND EXISTS (" +
            "SELECT m2 FROM ConversationMember m2 WHERE m2.conversation = c AND m2.user.id = :userId" +
            ") AND EXISTS (SELECT m3 FROM ConversationMember m3 WHERE m3.conversation = c AND m3.user.id = :otherUserId)"
    )
    Optional<Conversation> findDirectBetween(
        @Param("projectId") Long projectId,
        @Param("userId") Long userId,
        @Param("otherUserId") Long otherUserId
    );
}
