package com.ceos.beatbuddy.domain.coupon.domain;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.BaseTimeEntity;
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
@Table(
        name = "MemberCoupon",
        indexes = {
                @Index(name = "idx_member_coupon_date", columnList = "memberId, couponId, createdAt")
        }
)
public class MemberCoupon extends BaseTimeEntity {

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

    // 쿠폰 베뉴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venueId")
    private Venue venue;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    private LocalDateTime usedDate; // 쿠폰 사용 날짜

    public void setUsedDate(LocalDateTime now) {
        this.usedDate = now;
    }

    public enum CouponStatus {
        RECEIVED, USED
    }

    // 쿠폰 사용 처리
    public void markUsed() {
        this.status = CouponStatus.USED;
    }

    // 쿠폰 발급 엔티티 생성
    public static MemberCoupon toEntity(Venue venue, Member member, Coupon coupon) {
        return MemberCoupon.builder()
                .member(member)
                .coupon(coupon)
                .venue(venue)
                .usedDate(null) // 사용하지 않은 상태로 초기화
                .status(CouponStatus.RECEIVED)
                .build();
    }
}