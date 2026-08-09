package com.atstudio.atstudio.common.validation;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class TagNamePolicy {

    private static final Pattern EDGE_SPACE_SEPARATORS =
            Pattern.compile("^\\p{Zs}+|\\p{Zs}+$");
    private static final Pattern SPACE_SEPARATOR_RUN = Pattern.compile("\\p{Zs}+");
    private static final Pattern ALLOWED_NAME =
            Pattern.compile("^[\\p{IsHangul}A-Za-z0-9 &/'\\u2019()\\-]+$");

    private TagNamePolicy() {
    }

    public static String canonicalize(String rawName) {
        if (rawName == null) {
            return null;
        }

        String trimmed = EDGE_SPACE_SEPARATORS.matcher(rawName).replaceAll("");
        String collapsed = SPACE_SEPARATOR_RUN.matcher(trimmed).replaceAll(" ");
        return Normalizer.normalize(collapsed, Normalizer.Form.NFC);
    }

    public static boolean isWithinRawLimit(String rawName) {
        return rawName != null
                && rawName.codePointCount(0, rawName.length()) <= ValidationConstants.TAG_NAME_RAW_MAX;
    }

    public static boolean isValid(String canonicalName) {
        if (canonicalName == null || canonicalName.isEmpty()) {
            return false;
        }

        int characterCount = canonicalName.codePointCount(0, canonicalName.length());
        return characterCount <= ValidationConstants.TAG_NAME_MAX
                && ALLOWED_NAME.matcher(canonicalName).matches();
    }
}
