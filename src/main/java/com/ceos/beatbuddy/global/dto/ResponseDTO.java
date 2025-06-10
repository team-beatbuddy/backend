package com.ceos.beatbuddy.global.dto;

import com.ceos.beatbuddy.global.SuccessCode;
import lombok.Data;

@Data
public class ResponseDTO<T> {
    private Integer status;
    private String code;
    private String message;
    private T data;

    public ResponseDTO(SuccessCode successCode, T data) {
        this.status = successCode.getStatus().value();
        this.code = successCode.name();
        this.message = successCode.getMessage();
        this.data = data;
    }
}