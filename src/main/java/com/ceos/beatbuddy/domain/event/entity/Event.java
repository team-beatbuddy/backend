package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private String title;

    @Getter
    @Lob
    private String content;

    @Getter
    private LocalDate startDate;
    @Getter
    private LocalDate endDate;
    @Getter
    private String location;

    @Getter
    private String thumbImage;

    @ElementCollection
    @Setter
    @Getter
    private List<String> imageUrls;

    @Getter
    private int views;
    @Getter
    private int likes;

    @Column(nullable = false)
    @Getter
    private boolean receiveInfo = false; // 참석자 정보 수집 여부
    @Getter
    @Column(nullable = false)
    private boolean receiveName = false; // 이름 받을 건지
    @Getter
    @Column(nullable = false)
    private boolean receiveGender = false; // 성별 받을 건지
    @Getter
    @Column(nullable = false)
    private boolean receivePhoneNumber= false; // 전화번호 받을 건지
    @Getter
    @Column(nullable = false)
    private boolean receiveTotalCount= false; // 동행 인원 받을 건지
    @Getter
    @Column(nullable = false)
    private boolean receiveSNSId= false; // sns id 받을 건지
    @Getter
    @Column(nullable = false)
    private boolean receiveMoney= false; // 예약금 받을 건지

    @Getter
    @Setter
    private String depositAccount; // 사전 예약금 계좌번호
    @Getter
    @Setter
    private Integer depositAmount; // 사전 예약금 금액

    @Column(nullable = false)
    private boolean isVisible = true;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    @Getter
    private Member host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venueId")
    @Getter
    private Venue venue;  // 비트버디 등록된 장소



    public void increaseView() {
        this.views++;
    }

    public void setThumbImage(String imageUrl) {
        this.thumbImage = imageUrl;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public void updateEventInfo(String title, String content, LocalDate startDate,
                                LocalDate endDate, String location, Boolean isVisible) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
        if (location != null) this.location = location;
        if (isVisible != null) this.isVisible = isVisible;
    }

    public void updateReceiveSettings(Boolean receiveInfo,
                                      Boolean receiveName,
                                      Boolean receiveGender,
                                      Boolean receivePhoneNumber,
                                      Boolean receiveTotalCount,
                                      Boolean receiveSNSId) {
        if (receiveInfo != null) {
            this.receiveInfo = receiveInfo;
            if (!receiveInfo) {
                this.receiveName = false;
                this.receiveGender = false;
                this.receivePhoneNumber = false;
                this.receiveTotalCount = false;
                this.receiveSNSId = false;
                return;
            }
        }
        if (Boolean.TRUE.equals(this.receiveInfo)) {
            if (receiveName != null) this.receiveName = receiveName;
            if (receiveGender != null) this.receiveGender = receiveGender;
            if (receivePhoneNumber != null) this.receivePhoneNumber = receivePhoneNumber;
            if (receiveTotalCount != null) this.receiveTotalCount = receiveTotalCount;
            if (receiveSNSId != null) this.receiveSNSId = receiveSNSId;
        }
    }

    public void updateDepositSettings(Boolean receiveMoney, String depositAccount, Integer depositAmount) {
        if (receiveMoney != null) this.receiveMoney = receiveMoney;

        boolean finalReceiveMoney = receiveMoney != null ? receiveMoney : this.receiveMoney;

        if (finalReceiveMoney) {
            if (depositAccount != null) this.depositAccount = depositAccount;
            if (depositAmount != null) this.depositAmount = depositAmount;
        } else {
            this.depositAccount = null;
            this.depositAmount = null;
        }
    }
}
