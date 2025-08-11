package com.ceos.beatbuddy.domain.member.entity;

import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.member.constant.Gender;
import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventLike;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.type.SetType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loginId;


    private String nickname;
    private String realName;

    private Gender gender;

    @Convert(converter = RegionConverter.class)
    private List<Region> regions;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Column
    private LocalDateTime nicknameChangedAt;

    @Column(nullable = false)
    private int nicknameChangeCount = 0;

    @Version
    @Column(nullable = false)
    private Long version = 0L;


    @Builder.Default
    private Boolean setNewNickname = false;
    @Builder.Default
    private Boolean isLocationConsent = false;
    @Builder.Default
    private Boolean isMarketingConsent = false;

    private Long latestArchiveId;

    private String profileImage;

    @Column(length = 512)
    @Setter
    private String fcmToken;

    @Embedded
    @Builder.Default
    private BusinessInfo businessInfo = new BusinessInfo(
            null, false, null, null, false
    );

    @Embedded
    @Builder.Default
    private PostProfileInfo postProfileInfo = new PostProfileInfo(
            null, null
    );

    public void saveConsents(Boolean isLocationConsent, Boolean isMarketingConsent) {
        this.isLocationConsent = isLocationConsent;
        this.isMarketingConsent = isMarketingConsent;
    }

    public void saveNickname(String nickname) {
        this.nickname = nickname;
        this.setNewNickname = true;
    }

    public void saveRegions(List<Region> regions) {
        this.regions = regions;
    }

    public void saveLatestArchiveId(Long latestArchiveId) {this.latestArchiveId = latestArchiveId;}

    public void setBusinessMember() {
        this.role = Role.BUSINESS_NOT;
    }

    public void setRealName(String realName){
        this.realName = realName;
    }

    public void setDateOfBirthAndGender(String residentRegistration) {
        if (residentRegistration == null || residentRegistration.length() < 7) {
            throw new IllegalArgumentException("유효하지 않은 주민등록번호입니다.");
        }

        String birth = residentRegistration.substring(0, 6);
        char genderCode = residentRegistration.charAt(6);

        int yearPrefix;
        switch (genderCode) {
            case '1': case '2': yearPrefix = 1900; break;
            case '3': case '4': yearPrefix = 2000; break;
            case '5': case '6': yearPrefix = 1900; break; // 외국인
            case '7': case '8': yearPrefix = 2000; break; // 외국인
            default:
                throw new IllegalArgumentException("유효하지 않은 성별 코드입니다.");
        }

        int year = yearPrefix + Integer.parseInt(birth.substring(0, 2));
        int month = Integer.parseInt(birth.substring(2, 4));
        int day = Integer.parseInt(birth.substring(4, 6));

        this.businessInfo.saveBirth(LocalDate.of(year, month, day));
        this.gender = (genderCode % 2 == 1) ? Gender.TYPE1 : Gender.TYPE2;
    }

    public void setProfileImage(String image) {
        this.profileImage = image;
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    public void setNicknameChangedAt(LocalDateTime now) {
        this.nicknameChangedAt = now;
    }

    public void setNicknameChangeCount(int i) {
        this.nicknameChangeCount = i;
    }

    public void setNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public void setRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.role = role;
    }

    public void setPostProfileInfo(PostProfileInfo from) {
        if (from == null) {
            throw new IllegalArgumentException("PostProfileInfo cannot be null");
        }
        this.postProfileInfo = from;
    }
}
