package com.ceos.beatbuddy.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MyPageCouponList {
    private int totalCount; // 전체 쿠폰 개수
    private int totalPage; // 전체 페이지 수
    private int currentPage; // 현재 페이지 번호
    private int pageSize; // 페이지당 쿠폰 개수
    private List<MyPageCouponDTO> coupons; // 쿠폰 목록
}
