package com.ceos.beatbuddy.domain.coupon.dto;

import com.ceos.beatbuddy.domain.coupon.domain.MemberCoupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MyPageCouponDTO {
    private Long memberCouponId; // MemberCoupon ID
    private Long couponId;
    private Long memberId;
    private String couponName;
    private String couponContent;
    private String howToUse; // 쿠폰 사용 방법
    private String venueName;
    private LocalDateTime receivedDate; // 쿠폰을 받은 날짜와 시간
    private LocalDateTime usedDate; // 쿠폰을 사용한 날짜와 시간 (사용하지 않은 경우 null)
    private LocalDate expirationDate; // 쿠폰의 만료 날짜와 시간
    private String status; // "RECEIVED" or "USED"

    public static MyPageCouponDTO toDTO(MemberCoupon memberCoupon) {
        return MyPageCouponDTO.builder()
                .memberCouponId(memberCoupon.getId())
                .couponId(memberCoupon.getCoupon().getId())
                .couponName(memberCoupon.getCoupon().getName())
                .couponContent(memberCoupon.getCoupon().getContent())
                .howToUse(memberCoupon.getCoupon().getHowToUse())
                .memberId(memberCoupon.getMember().getId())
                .venueName(memberCoupon.getVenue().getKoreanName())
                .receivedDate(memberCoupon.getCreatedAt())
                .usedDate(memberCoupon.getStatus() == MemberCoupon.CouponStatus.USED ? memberCoupon.getUsedDate() : null)
                .expirationDate(memberCoupon.getCoupon().getExpireDate())
                .status(memberCoupon.getStatus().name())
                .build();
    }
}
