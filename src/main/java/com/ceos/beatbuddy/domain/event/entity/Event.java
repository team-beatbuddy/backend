package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

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

    @Getter
    private int views;
    @Getter
    private int likes;

    @Getter
    private boolean receiveInfo; // 참석자 정보 수집 여부
    @Getter
    private boolean receiveName = false; // 이름 받을 건지
    @Getter
    private boolean receiveGender = false;; // 성별 받을 건지
    @Getter
    private boolean receivePhoneNumber= false;; // 전화번호 받을 건지
    @Getter
    private boolean receiveTotalCount= false;; // 동행 인원 받을 건지
    @Getter
    private boolean receiveSNSId= false;; // sns id 받을 건지
    @Getter
    private boolean receiveMoney= false;; // 예약금 받을 건지

    private String depositAccount; // 사전 예약금 계좌번호
    private Integer depositAmount; // 사전 예약금 금액

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
}
