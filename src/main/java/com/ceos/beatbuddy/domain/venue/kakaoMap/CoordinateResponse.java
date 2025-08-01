package com.ceos.beatbuddy.domain.venue.kakaoMap;

import lombok.Getter;

@Getter
public class CoordinateResponse {
    private double x;
    private double y;

    public CoordinateResponse(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
