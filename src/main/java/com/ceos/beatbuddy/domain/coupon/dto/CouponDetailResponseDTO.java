package com.ceos.beatbuddy.domain.coupon.dto;

public class CouponDetailResponseDTO {
    private Long couponId;
    private String name;
    private String description;
    private String imageUrl;
    private int quota;
    private int usedQuota;
    private String startDate;
    private String endDate;

    public CouponDetailResponseDTO(Long couponId, String name, String description, String imageUrl, int quota, int usedQuota, String startDate, String endDate) {
        this.couponId = couponId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.quota = quota;
        this.usedQuota = usedQuota;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
}
