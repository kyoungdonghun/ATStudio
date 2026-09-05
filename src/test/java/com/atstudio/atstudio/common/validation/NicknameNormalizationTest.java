package com.atstudio.atstudio.common.validation;

import com.atstudio.atstudio.dto.user.CompleteProfileRequest;
import com.atstudio.atstudio.dto.user.RegisterRequest;
import com.atstudio.atstudio.dto.user.UpdateProfileRequest;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NicknameNormalizationTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Set<Integer> EDGE_CODE_POINTS = Set.of(
            0x0009, 0x000a, 0x000b, 0x000c, 0x000d, 0x0020, 0x00a0, 0x1680,
            0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005, 0x2006, 0x2007,
            0x2008, 0x2009, 0x200a, 0x2028, 0x2029, 0x202f, 0x205f, 0x3000, 0xfeff);

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void trimsExactlyTheEcmascriptWhitespaceEdgeSet() {
        RegisterRequest request = new RegisterRequest();
        for (int codePoint = 0; codePoint <= Character.MAX_VALUE; codePoint++) {
            String edge = String.valueOf((char) codePoint);
            String raw = edge + "AT_M" + edge;
            request.setNickname(raw);
            assertThat(request.getNickname())
                    .as("edge U+%04X", codePoint)
                    .isEqualTo(EDGE_CODE_POINTS.contains(codePoint) ? "AT_M" : raw);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {
            0x0009, 0x000a, 0x000b, 0x000c, 0x000d, 0x0020, 0x00a0, 0x1680,
            0x2000, 0x2001, 0x2002, 0x2003, 0x2004, 0x2005, 0x2006, 0x2007,
            0x2008, 0x2009, 0x200a, 0x2028, 0x2029, 0x202f, 0x205f, 0x3000, 0xfeff
    })
    void deserializesAllWriteDtosBeforeNicknameValidation(int codePoint) throws Exception {
        String edge = String.valueOf((char) codePoint);
        String json = MAPPER.writeValueAsString(Map.of("nickname", edge + "AT  M" + edge));
        RegisterRequest register = MAPPER.readValue(json, RegisterRequest.class);
        CompleteProfileRequest complete = MAPPER.readValue(json, CompleteProfileRequest.class);
        UpdateProfileRequest update = MAPPER.readValue(json, UpdateProfileRequest.class);

        assertThat(register.getNickname()).isEqualTo("AT  M");
        assertThat(complete.getNickname()).isEqualTo("AT  M");
        assertThat(update.getNickname()).isEqualTo("AT  M");
        assertThat(VALIDATOR.validateProperty(register, "nickname")).isEmpty();
        assertThat(VALIDATOR.validateProperty(complete, "nickname")).isEmpty();
        assertThat(VALIDATOR.validateProperty(update, "nickname")).isEmpty();

        User user = User.builder().nickname("old").build();
        user.updateProfile(edge + "AT  M" + edge, null, null, null, null);
        assertThat(user.getNickname()).isEqualTo("AT  M");
        user.completeProfile(edge + "New  M" + edge, "010-1234-5678", null,
                UserJob.EDITOR, UserType.INDIVIDUAL, null);
        assertThat(user.getNickname()).isEqualTo("New  M");
    }

    @ParameterizedTest
    @ValueSource(strings = {"\t", "\n", "\u00a0", "\u2007", "\u202f", "\ufeff"})
    void preservesAndRejectsUnsupportedInternalWhitespace(String whitespace) {
        String raw = "AT" + whitespace + "M";
        RegisterRequest register = new RegisterRequest();
        CompleteProfileRequest complete = new CompleteProfileRequest();
        UpdateProfileRequest update = new UpdateProfileRequest();
        register.setNickname(raw);
        complete.setNickname(raw);
        update.setNickname(raw);

        assertThat(register.getNickname()).isEqualTo(raw);
        assertThat(complete.getNickname()).isEqualTo(raw);
        assertThat(update.getNickname()).isEqualTo(raw);
        assertThat(VALIDATOR.validateProperty(register, "nickname")).isNotEmpty();
        assertThat(VALIDATOR.validateProperty(complete, "nickname")).isNotEmpty();
        assertThat(VALIDATOR.validateProperty(update, "nickname")).isNotEmpty();
    }

    @Test
    void keepsNullAndRejectsAnAllWhitespaceNickname() {
        RegisterRequest register = new RegisterRequest();
        register.setNickname(null);
        assertThat(register.getNickname()).isNull();
        register.setNickname("\u00a0\u2007\u202f\ufeff");
        assertThat(register.getNickname()).isEmpty();
        assertThat(VALIDATOR.validateProperty(register, "nickname")).isNotEmpty();
    }
}
