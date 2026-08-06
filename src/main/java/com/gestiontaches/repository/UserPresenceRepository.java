package com.gestiontaches.repository;

import com.gestiontaches.domain.UserPresence;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link UserPresence} entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserPresenceRepository extends JpaRepository<UserPresence, Long> {
    Optional<UserPresence> findByUserId(Long userId);

    @Query("SELECT p FROM UserPresence p LEFT JOIN FETCH p.user WHERE p.user.id IN :userIds")
    List<UserPresence> findAllByUserIds(@Param("userIds") List<Long> userIds);
}
