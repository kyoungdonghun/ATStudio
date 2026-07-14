package com.atstudio.atstudio.security;

import com.atstudio.atstudio.config.AcceptanceProperties;
import com.atstudio.atstudio.config.TrustedClientIdentityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TrustedClientIdentityResolver {

    public static final String INTERNAL_CLIENT_IP_HEADER = "X-ATStudio-Client-IP";
    private static final String UNKNOWN_CLIENT = "unknown";
    private static final int MAX_IP_LITERAL_LENGTH = 64;

    private final TrustedClientIdentityProperties properties;
    private final AcceptanceProperties acceptanceProperties;

    public String resolve(HttpServletRequest request) {
        String directPeer = parseLiteral(request.getRemoteAddr()).orElse(UNKNOWN_CLIENT);
        if ((!properties.isEnabled() && !acceptanceProperties.isEnabled()) || !isTrustedLoopback(directPeer)) {
            return directPeer;
        }

        Enumeration<String> assertedValues = request.getHeaders(INTERNAL_CLIENT_IP_HEADER);
        if (assertedValues == null || !assertedValues.hasMoreElements()) {
            return directPeer;
        }

        String assertedValue = assertedValues.nextElement();
        if (assertedValues.hasMoreElements()) {
            return directPeer;
        }

        return parseLiteral(assertedValue).orElse(directPeer);
    }

    private boolean isTrustedLoopback(String directPeer) {
        if (!isLoopback(directPeer)) {
            return false;
        }

        return properties.getTrustedProxyAddresses().stream()
                .map(TrustedClientIdentityResolver::parseLiteral)
                .flatMap(Optional::stream)
                .anyMatch(directPeer::equals);
    }

    private static boolean isLoopback(String literal) {
        try {
            return InetAddress.getByName(literal).isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    static Optional<String> parseLiteral(String value) {
        if (value == null
                || value.isEmpty()
                || value.length() > MAX_IP_LITERAL_LENGTH
                || !value.equals(value.trim())
                || value.chars().anyMatch(Character::isWhitespace)
                || value.indexOf(',') >= 0
                || value.indexOf('%') >= 0
                || value.indexOf('[') >= 0
                || value.indexOf(']') >= 0) {
            return Optional.empty();
        }

        if (value.indexOf(':') >= 0) {
            return parseIpv6(value);
        }
        return parseIpv4(value);
    }

    private static Optional<String> parseIpv4(String value) {
        String[] parts = value.split("\\.", -1);
        if (parts.length != 4) {
            return Optional.empty();
        }

        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()
                    || (part.length() > 1 && part.charAt(0) == '0')
                    || !part.chars().allMatch(Character::isDigit)) {
                return Optional.empty();
            }

            int octet;
            try {
                octet = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
            if (octet > 255) {
                return Optional.empty();
            }

            if (i > 0) {
                normalized.append('.');
            }
            normalized.append(octet);
        }
        return Optional.of(normalized.toString());
    }

    private static Optional<String> parseIpv6(String value) {
        if (!value.matches("[0-9A-Fa-f:.]+")) {
            return Optional.empty();
        }

        try {
            InetAddress address = InetAddress.getByName(value);
            if (!(address instanceof Inet6Address)) {
                return Optional.empty();
            }
            return Optional.of(address.getHostAddress());
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }
}
