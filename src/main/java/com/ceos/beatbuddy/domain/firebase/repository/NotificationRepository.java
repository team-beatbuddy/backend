package com.ceos.beatbuddy.domain.firebase.repository;

import com.ceos.beatbuddy.domain.firebase.entity.Notification;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByReceiver(Member member, Pageable pageable);
}
