package com.gestiontaches.repository;

import com.gestiontaches.domain.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link MessageReaction} entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {
    void deleteByMessageId(Long messageId);
}
