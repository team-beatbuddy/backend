package com.ceos.beatbuddy.domain.coupon.domain;


import com.ceos.beatbuddy.domain.coupon.exception.CouponErrorCode;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.CustomException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;              // 쿠폰 이름

    private LocalDate expireDate;     // 쿠폰 만료일

    @Lob
    private String howToUse;          // 사용 방법 (팝업 내용)

    @Lob
    private String notes;             // 유의사항 (팝업 내용)

    private Boolean active;           // 사용 가능 여부 (ex. soft delete 용)

    private int quota;                // 쿠폰 발급 수량

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venueId")
    private Venue venue;

    @Enumerated(EnumType.STRING)
    private CouponPolicy policy;

    public enum CouponPolicy {
        ONCE,       // 한 번만 수령 가능
        DAILY       // 매일 수령 가능
    }

    public static CouponPolicy to(String value) {
        for (CouponPolicy policy : CouponPolicy.values()) {
            if (policy.name().equalsIgnoreCase(value)) {
                return policy;
            }
        }
        throw new CustomException(CouponErrorCode.COUPON_INVALID_POLICY);
    }
}

