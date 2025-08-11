package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.event.dto.EventUpdateRequestDTO;
import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventLike;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import com.ceos.beatbuddy.global.CustomException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "Event",
        indexes = {
                @Index(name = "idx_event_status_start", columnList = "status, startDate"),
                @Index(name = "idx_event_status_region_start", columnList = "status, region, startDate"),
                @Index(name = "idx_event_status_end", columnList = "status, endDate")
        }
)
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;

    private int entranceFee = 0; // 입장료
    private String entranceNotice; // 입장료 공지
    private String notice;
    private boolean isFreeEntrance; // 무료 입장 여부

    private String thumbImage;

    @ElementCollection
    @Setter
    private List<String> imageUrls;

    private int views;
    private int likes;

    @Column(nullable = false)
    private boolean receiveInfo = false; // 참석자 정보 수집 여부
    @Column(nullable = false)
    private boolean receiveName = false; // 이름 받을 건지
    @Column(nullable = false)
    private boolean receiveGender = false; // 성별 받을 건지
    @Column(nullable = false)
    private boolean receivePhoneNumber= false; // 전화번호 받을 건지
    @Column(nullable = false)
    private boolean receiveTotalCount= false; // 동행 인원 받을 건지
    @Column(nullable = false)
    private boolean receiveSNSId= false; // sns id 받을 건지
    @Column(nullable = false)
    private boolean receiveMoney= false; // 예약금 받을 건지

    @Setter
    private String depositAccount; // 사전 예약금 계좌번호
    @Setter
    private Integer depositAmount; // 사전 예약금 금액

    @Column(nullable = false)
    private boolean isVisible = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venueId")
    private Venue venue;  // 비트버디 등록된 장소

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    @OneToMany(mappedBy = "event", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<EventLike> eventLikes = new ArrayList<>();

    @OneToMany(mappedBy = "event", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<EventComment> eventComments = new ArrayList<>();

    @OneToMany(mappedBy = "event", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<EventAttendance> eventAttendances = new ArrayList<>();

    public enum Region {
        홍대,
        강남_신사,
        압구정_로데오,
        이태원,
        기타;

        public static Region of(String value) {
            if (value == null) {
                throw new CustomException(EventErrorCode.REGION_NOT_EXIST);
            }
            
            // 정규화: 공백, 특수문자를 언더스코어로 통일
            String normalized = value.trim()
                    .replace(" ", "_")
                    .replace("/", "_")
                    .replace(".", "_")
                    .replace("-", "_");
            
            try {
                return Region.valueOf(normalized);
            } catch (IllegalArgumentException e) {
                throw new CustomException(EventErrorCode.REGION_NOT_EXIST);
            }
        }
    }

    public void setThumbImage(String imageUrl) {
        this.thumbImage = imageUrl;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public void updateEventInfo(EventUpdateRequestDTO dto) {
        // 1. 무료 여부 먼저 처리
        if (dto.getIsFreeEntrance() != null) {
            this.isFreeEntrance = dto.getIsFreeEntrance();
            if (dto.getIsFreeEntrance()) {
                this.entranceFee = 0; // 무료 체크되면 무조건 0원
            }
        }

        // 2. 금액이 온 경우
        if (dto.getEntranceFee() != null) {
            // 무료 상태일 경우 무시 (불변성 유지)
            if (!this.isFreeEntrance) {
                this.entranceFee = dto.getEntranceFee();
            }
        }

        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            this.title = dto.getTitle();
        }
        if (dto.getContent() != null) {
            this.content = dto.getContent();
        }
        // 날짜 업데이트 (임시 변수에 저장 후 검증)
        LocalDateTime newStartDate = dto.getStartDate() != null ? dto.getStartDate() : this.startDate;
        LocalDateTime newEndDate = dto.getEndDate() != null ? dto.getEndDate() : this.endDate;
        
        // 날짜 유효성 검증 (startDate > endDate 방지)
        if (newStartDate != null && newEndDate != null && newStartDate.isAfter(newEndDate)) {
            throw new CustomException(EventErrorCode.INVALID_DATE_RANGE);
        }
        
        // 검증 통과 시 실제 업데이트
        if (dto.getStartDate() != null) {
            this.startDate = dto.getStartDate();
        }
        if (dto.getEndDate() != null) {
            this.endDate = dto.getEndDate();
        }

        // 날짜 변경 후 상태 자동 업데이트
        updateEventStatusByDate();

        if (dto.getLocation() != null) {
            this.location = dto.getLocation();
        }
        if (dto.getIsVisible() != null) {
            this.isVisible = dto.getIsVisible();
        }
        if (dto.getEntranceNotice() != null && !dto.getEntranceNotice().trim().isEmpty()) {
            this.entranceNotice = dto.getEntranceNotice();
        }
        if (dto.getNotice() != null && !dto.getNotice().trim().isEmpty()) {
            this.notice = dto.getNotice();
        }
        if (dto.getRegion() != null) {
            this.region = Region.of(dto.getRegion());
        }
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

    public void updateEventStatusByDate() {
        LocalDateTime today = LocalDateTime.now();
        if (startDate != null && endDate != null) {
            System.out.println("=== Event Status Debug ===");
            System.out.println("Current time: " + today);
            System.out.println("Start date: " + startDate);
            System.out.println("End date: " + endDate);
            System.out.println("startDate.isAfter(today): " + startDate.isAfter(today));
            System.out.println("endDate.isBefore(today): " + endDate.isBefore(today));
            
            // startDate <= 오늘 <= endDate 인 경우 NOW
            if (!startDate.isAfter(today) && !endDate.isBefore(today)) {
                this.status = EventStatus.NOW;
                System.out.println("Status set to: NOW");
            } else if (startDate.isAfter(today)) {
                // startDate > 오늘 인 경우 UPCOMING
                this.status = EventStatus.UPCOMING;
                System.out.println("Status set to: UPCOMING");
            } else {
                // endDate < 오늘 인 경우 PAST
                this.status = EventStatus.PAST;
                System.out.println("Status set to: PAST");
            }
            System.out.println("========================");
        }
    }
}
