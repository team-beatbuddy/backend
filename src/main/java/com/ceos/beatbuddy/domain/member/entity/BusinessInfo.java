package com.ceos.beatbuddy.domain.member.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "사업자 정보")
public class BusinessInfo {
    @Schema(description = "사업자 이름", example = "홍길동의 카페")
    private String businessName;

    @Schema(description = "사업자 승인 여부", example = "false")
    private boolean isApproved = false;
    @Schema(description = "사업자 전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "사업자 통신사", example = "SKT")
    private String telCarrier; // 사업자 통신사
    @Schema(description = "사업자 생년월일", example = "1990-01-01")
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    @Schema(description = "본인 인증 완료 여부", example = "false")
    private boolean isVerified = false; // 본인 인증 완료 되었는지

    public void saveTelCarrier(String telCarrier) {
        this.telCarrier = telCarrier;
    }
    public void savePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void saveBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void saveBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void updateVerify() {
        this.isVerified = true;
    }

    public void setApproved(boolean b) {
        this.isApproved = b;
    }
}