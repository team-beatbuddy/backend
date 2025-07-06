package com.ceos.beatbuddy.domain.coupon.domain;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;

    // 쿠폰
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couponId")
    private Coupon coupon;

    // 쿠폰 사용처 (업장)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venueId")
    private Venue venue;

    private LocalDate receivedDate;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    public enum CouponStatus {
        RECEIVED, USED
    }

    // 쿠폰 사용 처리
    public void markUsed() {
        this.status = CouponStatus.USED;
    }

    // 쿠폰 발급 엔티티 생성
    public static MemberCoupon toEntity(Member member, Coupon coupon, Venue venue) {
        return MemberCoupon.builder()
                .member(member)
                .coupon(coupon)
                .venue(venue)
                .receivedDate(LocalDate.now())
                .status(CouponStatus.RECEIVED)
                .build();
    }
}