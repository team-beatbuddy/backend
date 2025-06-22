package com.ceos.beatbuddy.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessInfo {
    private String businessName;
    @Column(nullable = false)
    private boolean isApproved = false;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private boolean isVerified = false; // 본인 인증 완료 되었는지

    public void savePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void saveBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void saveBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void saveVerify() {
        this.isVerified = true;
    }
}