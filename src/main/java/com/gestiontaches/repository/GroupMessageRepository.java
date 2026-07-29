package com.gestiontaches.repository;

import com.gestiontaches.domain.GroupMessage;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@SuppressWarnings("unused")
@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    List<GroupMessage> findByProjectIdOrderByCreatedAtAsc(Long projectId);

    @Query(
        "SELECT gm FROM GroupMessage gm WHERE gm.project.id = :projectId AND (" +
            "gm.recipient IS NULL OR gm.sender.id = :userId OR gm.recipient.id = :userId" +
            ") ORDER BY gm.createdAt ASC"
    )
    List<GroupMessage> findVisibleMessages(
        @org.springframework.data.repository.query.Param("projectId") Long projectId,
        @org.springframework.data.repository.query.Param("userId") Long userId
    );
}
