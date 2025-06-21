package com.ceos.beatbuddy.domain.member.entity;

import com.ceos.beatbuddy.domain.archive.entity.Archive;
import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.heartbeat.entity.Heartbeat;
import com.ceos.beatbuddy.domain.member.constant.Gender;
import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

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

    private String role;

    @Builder.Default
    private Boolean setNewNickname = false;
    @Builder.Default
    private Boolean isLocationConsent = false;
    @Builder.Default
    private Boolean isMarketingConsent = false;

    private Long latestArchiveId;

    private String profileImage;

    @Embedded
    @Setter
    private BusinessInfo businessInfo;


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
        this.role = "BUSINESS";
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

}
