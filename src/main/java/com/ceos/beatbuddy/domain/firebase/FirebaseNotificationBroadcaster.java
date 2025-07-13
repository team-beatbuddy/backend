package com.ceos.beatbuddy.domain.firebase;

import com.ceos.beatbuddy.domain.firebase.service.FirebaseNotificationSender;
import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FirebaseNotificationBroadcaster {

    private final MemberRepository memberRepository;
    private final FirebaseNotificationSender firebaseNotificationSender;

    public void broadcast(String role, NotificationPayload payload) {
        List<Member> members;

        switch (role.toUpperCase()) {
            case "USER" -> members = memberRepository.findAllByRole(Role.USER);
            case "BUSINESS" -> members = memberRepository.findAllByRole(Role.BUSINESS);
            case "ADMIN" -> members = memberRepository.findAllByRole(Role.ADMIN);
            default -> members = memberRepository.findAll(); // "ALL"
        }

        for (Member member : members) {
            if (member.getFcmToken() != null && !member.getFcmToken().isBlank()) {
                firebaseNotificationSender.send(member.getFcmToken(), payload);
            }
        }
    }
}
