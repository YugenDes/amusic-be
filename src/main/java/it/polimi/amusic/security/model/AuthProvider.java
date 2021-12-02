package it.polimi.amusic.security.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum AuthProvider {
    GITHUB("github.com"), FACEBOOK("facebook.com"), GOOGLE("google.com"), AMUSIC("password");

    private final String firebaseClaim;

    AuthProvider(String firebaseClaim) {
        this.firebaseClaim = firebaseClaim;
    }

    public static AuthProvider parseValueOf(String value) {
        return Arrays.stream(AuthProvider.values())
                .filter(authProvider -> StringUtils.containsIgnoreCase(authProvider.firebaseClaim, value))
                .findFirst()
                .orElse(AuthProvider.AMUSIC);
    }
}
