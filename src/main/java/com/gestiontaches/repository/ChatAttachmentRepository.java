package com.gestiontaches.repository;

import com.gestiontaches.domain.ChatAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link ChatAttachment} entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, Long> {
    void deleteByMessageId(Long messageId);
}
