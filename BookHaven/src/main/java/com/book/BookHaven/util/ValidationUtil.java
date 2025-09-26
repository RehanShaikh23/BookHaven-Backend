package com.book.BookHaven.util;

import java.net.URL;

public class ValidationUtil {
    public static boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
