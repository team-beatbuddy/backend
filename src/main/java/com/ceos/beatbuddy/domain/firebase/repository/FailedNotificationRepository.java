package com.ceos.beatbuddy.domain.firebase.repository;

import com.ceos.beatbuddy.domain.firebase.entity.FailedNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FailedNotificationRepository extends JpaRepository<FailedNotification, Long> {

    List<FailedNotification> findTop100ByResolvedFalseAndRetryCountLessThanOrderByLastTriedAtAsc(int i);

    Optional<FailedNotification> findByTargetTokenAndPayloadJson(String token, String payloadJson);
}
