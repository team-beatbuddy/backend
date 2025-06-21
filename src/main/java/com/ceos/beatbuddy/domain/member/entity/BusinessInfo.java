package com.ceos.beatbuddy.domain.member.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDate;

@Embeddable
@Getter
public class BusinessInfo {
    private String businessName;
    private Boolean isApproved;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Boolean isVerified; // 본인 인증 완료 되었는지

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