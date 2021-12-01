package it.polimi.amusic.security.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum AuthProvider {
    FACEBOOK, GOOGLE, AMUSIC;

    public static AuthProvider parseValueOf(String value) {
        return Arrays.stream(AuthProvider.values())
                .map(Enum::name)
                .filter(authProviderName -> StringUtils.containsIgnoreCase(authProviderName, value))
                .map(AuthProvider::valueOf)
                .findFirst()
                .orElse(AuthProvider.AMUSIC);
    }
}
