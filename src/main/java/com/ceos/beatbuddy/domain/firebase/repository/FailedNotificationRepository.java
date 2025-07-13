package com.ceos.beatbuddy.domain.firebase.repository;

import com.ceos.beatbuddy.domain.firebase.entity.FailedNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedNotificationRepository extends JpaRepository<FailedNotification, Long> {
    // Custom query methods can be defined here if needed
    // For example, to find unresolved notifications or those with a specific retry count
    List<FailedNotification> findByResolvedFalse();
    List<FailedNotification> findByRetryCountLessThan(int maxRetries);

    List<FailedNotification> findTop100ByResolvedFalseAndRetryCountLessThanOrderByLastTriedAtAsc(int i);
}
