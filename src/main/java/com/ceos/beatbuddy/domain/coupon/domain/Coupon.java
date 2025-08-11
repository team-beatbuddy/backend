package com.ceos.beatbuddy.domain.coupon.domain;


import com.ceos.beatbuddy.domain.coupon.dto.CouponUpdateRequestDTO;
import com.ceos.beatbuddy.domain.coupon.exception.CouponErrorCode;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import com.ceos.beatbuddy.global.CustomException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;              // 쿠폰 이름
    private String content;         // 쿠폰 내용 (팝업 제목)

    private LocalDate expireDate;     // 쿠폰 만료일

    @Lob
    private String howToUse;          // 사용 방법 (팝업 내용)

    @Lob
    private String notes;             // 유의사항 (팝업 내용)

    private Boolean active;           // 사용 가능 여부 (ex. soft delete 용)

    private int quota;                // 쿠폰 발급 수량

    @ManyToMany
    @JoinTable(
            name = "couponVenue",
            joinColumns = @JoinColumn(name = "couponId"),
            inverseJoinColumns = @JoinColumn(name = "venueId")
    )
    private List<Venue> venues = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private CouponPolicy policy;

    private Integer maxReceiveCountPerUser; // 사용자당 최대 수령 가능 횟수
    private Integer sameVenueUse; // 같은 장소에서 사용 가능한 최대 횟수

    public enum CouponPolicy {
        ONCE,       // 한 번만 수령 가능
        DAILY,       // 매일 수령 가능
        WEEKLY,      // 매주 수령 가능
    }

    public static CouponPolicy to(String value) {
        for (CouponPolicy policy : CouponPolicy.values()) {
            if (policy.name().equalsIgnoreCase(value)) {
                return policy;
            }
        }
        throw new CustomException(CouponErrorCode.COUPON_INVALID_POLICY);
    }

    public void updateFromRequest(CouponUpdateRequestDTO dto, List<Venue> venues) {
        if (dto.getName() != null) {
            this.name = dto.getName();
        }
        if (dto.getContent() != null) {
            this.content = dto.getContent();
        }
        if (dto.getHowToUse() != null) {
            this.howToUse = dto.getHowToUse();
        }
        if (dto.getNotes() != null) {
            this.notes = dto.getNotes();
        }
        if (dto.getExpireDate() != null) {
            this.expireDate = dto.getExpireDate();
        }
        if (dto.getPolicy() != null) {
            this.policy = Coupon.to(dto.getPolicy());
        }
        if (dto.getQuota() != null) {
            this.quota = dto.getQuota();
        }
        if (dto.getMaxReceiveCountPerUser() != null) {
            this.maxReceiveCountPerUser = dto.getMaxReceiveCountPerUser();
        }
        if (dto.getSameVenueUse() != null) {
            this.sameVenueUse = dto.getSameVenueUse();
        }
        if (venues != null && !venues.isEmpty()) {
            this.venues = venues;
        }
    }

}

