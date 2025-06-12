package com.ceos.beatbuddy.global;

import org.springframework.http.HttpStatus;

public interface ApiCode {
    // This method will provide the HTTP status associated with the error
    HttpStatus getStatus();

    // This method will provide the human-readable message for the error
    String getMessage();

    // This method will provide a unique string code for the error (e.g., "MEMBER_NOT_EXIST")
    String name(); // Enum's name() method can serve as the unique code
}