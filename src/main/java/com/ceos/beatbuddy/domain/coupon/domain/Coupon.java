package com.ceos.beatbuddy.domain.coupon.domain;


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

    private LocalDateTime expireDate;     // 쿠폰 만료일

    @Lob
    private String howToUse;          // 사용 방법 (팝업 내용)

    @Lob
    private String notes;             // 유의사항 (팝업 내용)

    private Boolean active;           // 사용 가능 여부 (ex. soft delete 용)

    @Enumerated(EnumType.STRING)
    private CouponPolicy policy;

    public enum CouponPolicy {
        ONCE,       // 한 번만 수령 가능
        DAILY       // 매일 수령 가능
    }
}

