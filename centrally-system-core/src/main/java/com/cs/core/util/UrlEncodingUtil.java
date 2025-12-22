package com.cs.core.util;

import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class UrlEncodingUtil {

    // 문자열 인코딩
    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // 문자열 디코딩
    public static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
