package com.atstudio.atstudio.common.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TagNamePolicyTest {

    @Test
    void canonicalizationTrimsCollapsesSpaceSeparatorsAndAppliesNfcInOrder() {
        String rawName = "  \u1100\u1161\u2003\u00a0Beat  ";

        assertThat(TagNamePolicy.canonicalize(rawName)).isEqualTo("가 Beat");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "K-Pop",
            "R&B",
            "Hip Hop/R&B",
            "Artist's Choice",
            "Children’s Music",
            "Electronic Dance Music (EDM)"
    })
    void acceptsEveryApprovedPunctuationMark(String name) {
        assertThat(TagNamePolicy.isValid(TagNamePolicy.canonicalize(name))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "#Usage",
            "Smile😀",
            "Rock\tRoll",
            "Rock\nRoll",
            "R.B",
            "R_B",
            "R+B",
            "[Live]"
    })
    void rejectsHashEmojiControlsAndUnapprovedPunctuation(String name) {
        assertThat(TagNamePolicy.isValid(TagNamePolicy.canonicalize(name))).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\u2003\u00a0"})
    void rejectsBlankNames(String name) {
        assertThat(TagNamePolicy.isValid(TagNamePolicy.canonicalize(name))).isFalse();
    }

    @Test
    void rawLimitUsesUnicodeCodePoints() {
        assertThat(TagNamePolicy.isWithinRawLimit("가".repeat(200))).isTrue();
        assertThat(TagNamePolicy.isWithinRawLimit("가".repeat(201))).isFalse();
        assertThat(TagNamePolicy.isWithinRawLimit(null)).isFalse();
    }

    @Test
    void finalLimitUsesUnicodeCodePoints() {
        assertThat(TagNamePolicy.isValid("가".repeat(50))).isTrue();
        assertThat(TagNamePolicy.isValid("가".repeat(51))).isFalse();
    }
}
