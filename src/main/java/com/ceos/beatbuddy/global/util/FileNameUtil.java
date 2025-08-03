package com.ceos.beatbuddy.global.util;

public class FileNameUtil {
    public static String extractFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1 || lastSlashIndex == url.length() - 1) {
            throw new IllegalArgumentException("Invalid URL format: " + url);
        }
        return url.substring(lastSlashIndex + 1);
    }
}