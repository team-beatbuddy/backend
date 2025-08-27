package com.ceos.beatbuddy.domain.firebase.repository;

import com.ceos.beatbuddy.domain.firebase.entity.Notification;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.receiver = :member ORDER BY n.createdAt DESC")
    Page<Notification> findByReceiver(@Param("member") Member member, Pageable pageable);
}
