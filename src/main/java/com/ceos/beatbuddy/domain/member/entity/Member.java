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

import java.time.LocalDate;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loginId;


    private String nickname;
    private String realName;

    private Gender gender;

    @Convert(converter = RegionConverter.class)
    private List<Region> regions;

    private LocalDate dateOfBirth;

    private String role;

    @Builder.Default
    private Boolean setNewNickname = false;
    @Builder.Default
    private Boolean isAdult= false;
    @Builder.Default
    private Boolean isLocationConsent = false;
    @Builder.Default
    private Boolean isMarketingConsent = false;

    private Long latestArchiveId;

    private String phoneNumber;
    @Builder.Default
    private Boolean isVerified = false; // 본인인증이 되었는지
    @Builder.Default
    private Boolean isApproved = false; // 관리자 승인을 받앗는지 (비즈니스만)
    private String businessName;


    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberGenre> memberGenres;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberMood> memberMoods;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Archive> archives;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Heartbeat> heartbeats;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;




    public void saveConsents(Boolean isLocationConsent, Boolean isMarketingConsent) {
        this.isLocationConsent = isLocationConsent;
        this.isMarketingConsent = isMarketingConsent;
    }

    public void saveNickname(String nickname) {
        this.nickname = nickname;
        this.setNewNickname = true;
    }

    public void saveBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void saveRegions(List<Region> regions) {
        this.regions = regions;
    }

    public void setAdultUser(){
        this.isAdult = true;
    }

    public void saveLatestArchiveId(Long latestArchiveId) {this.latestArchiveId = latestArchiveId;}

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

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

        this.dateOfBirth = LocalDate.of(year, month, day);
        this.gender = (genderCode % 2 == 1) ? Gender.TYPE1 : Gender.TYPE2;
    }

    public void saveVerify() {
        this.isVerified = true;
    }

}
