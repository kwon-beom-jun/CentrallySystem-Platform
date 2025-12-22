package com.cs.auth.enums;

public enum SocialProvider {
    GOOGLE, KAKAO;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
