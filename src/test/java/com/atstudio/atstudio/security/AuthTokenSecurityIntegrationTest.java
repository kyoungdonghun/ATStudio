package com.atstudio.atstudio.security;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.config.JwtConfig;
import com.atstudio.atstudio.dto.auth.AuthResponse;
import com.atstudio.atstudio.dto.auth.RefreshRequest;
import com.atstudio.atstudio.entity.PasswordResetToken;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.SocialProvider;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserJob;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.EmailVerificationTokenRepository;
import com.atstudio.atstudio.repository.PasswordResetTokenRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.service.EmailService;
import com.atstudio.atstudio.service.SiteSettingService;
import com.atstudio.atstudio.service.UserService;
import com.atstudio.atstudio.service.auth.AuthService;
import com.atstudio.atstudio.service.auth.OAuth2Service;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthTokenSecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JwtTokenProvider tokens;
    @Autowired JwtConfig jwtConfig;
    @MockitoBean AuthService controllerAuthService;
    @MockitoBean UserService userService;
    @MockitoBean SiteSettingService siteSettingService;
    @MockitoBean CustomUserDetailsService userDetailsService;

    private UserRepository users;
    private OAuth2Service oauth;
    private AuthService auth;
    private User user;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        oauth = mock(OAuth2Service.class);
        auth = new AuthService(mock(AuthenticationManager.class), tokens, users, oauth,
                mock(PasswordLoginPolicy.class));
        user = User.builder().id(71L).nickname("token-fixture").email("token@example.test")
                .password("encoded-old").isVerified(true).build();
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
    }

    @Test
    void rotationRejectsTheRealPriorHashWithoutRevokingTheReplacement() throws Exception {
        String oldRefresh = issueRefresh();
        String access = tokens.generateAccessToken(user.getId(), UserRole.USER);
        AuthResponse replacement = auth.refresh(request(oldRefresh));
        assertThat(replacement.refreshToken()).isNotEqualTo(oldRefresh);
        assertThat(user.getRefreshToken()).isEqualTo(hash(replacement.refreshToken()));
        rejectRefresh(oldRefresh, BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
        assertThat(user.getRefreshToken()).isEqualTo(hash(replacement.refreshToken()));
        rejectBearer(oldRefresh);
        rejectBearer(replacement.refreshToken());
        assertNormalAccess(access);
    }

    @Test
    void logoutRejectsRealRefreshAsRefreshAndAsApiBearerButKeepsAccessTtl() throws Exception {
        String refresh = issueRefresh();
        String access = tokens.generateAccessToken(user.getId(), UserRole.ADMIN);
        auth.logout(user.getId());
        assertThat(user.getRefreshToken()).isNull();
        rejectRefresh(refresh, BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
        rejectBearer(refresh);
        // Authorization still comes from the current DB role, not the JWT role claim.
        assertNormalAccess(access);
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + access))
                .andExpect(status().isForbidden());
    }

    @Test
    void passwordResetRejectsRealRefreshOnBothSurfacesWithoutExtendingAccessRevocation() throws Exception {
        String refresh = issueRefresh();
        String access = tokens.generateAccessToken(user.getId(), UserRole.USER);
        PasswordResetTokenRepository resets = mock(PasswordResetTokenRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        PasswordResetToken reset = PasswordResetToken.builder().user(user).token("reset-fixture")
                .expiresAt(LocalDateTime.now().plusMinutes(5)).build();
        when(resets.findUserIdByToken("reset-fixture")).thenReturn(Optional.of(user.getId()));
        when(resets.findByTokenForUpdate("reset-fixture", user.getId())).thenReturn(Optional.of(reset));
        when(encoder.encode("new-fixture-password")).thenReturn("encoded-new");
        EmailService email = new EmailService(mock(JavaMailSender.class),
                mock(EmailVerificationTokenRepository.class), resets, users, encoder,
                mock(PasswordLoginPolicy.class));

        email.resetPassword("reset-fixture", "new-fixture-password");

        assertThat(reset.isUsed()).isTrue();
        assertThat(user.getPassword()).isEqualTo("encoded-new");
        assertThat(user.getRefreshToken()).isNull();
        rejectRefresh(refresh, BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
        rejectBearer(refresh);
        assertNormalAccess(access);
    }

    @Test
    void accessAndTypelessTokensCannotRefreshEvenWhenTheirExactHashIsStored() throws Exception {
        String access = tokens.generateAccessToken(user.getId(), UserRole.USER);
        String legacy = Jwts.builder().subject(user.getId().toString())
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getSecret()))).compact();
        for (String token : new String[]{access, legacy}) {
            user.updateRefreshToken(hash(token));
            rejectRefresh(token, BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
            assertThat(user.getRefreshToken()).isEqualTo(hash(token));
        }
        verify(users, never()).findByIdForUpdate(anyLong());
        rejectBearer(legacy);
    }

    @Test
    void socialLifecycleRefreshesBeforeAndAfterProfileCompletionWithoutVerifyingEmail() throws Exception {
        user = User.builder().id(72L).nickname("social-fixture").email("untrusted@example.test")
                .password(null).build();
        when(users.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(oauth.processSocialLogin(SocialProvider.GOOGLE, "synthetic-code", "synthetic-verifier"))
                .thenReturn(user);
        var login = auth.socialLogin(SocialProvider.GOOGLE, "synthetic-code", "synthetic-verifier");
        assertThat(login.isProfileComplete()).isFalse();
        AuthResponse first = auth.refresh(request(login.refreshToken()));
        user.completeProfile("social-fixture", "01000000000", null, UserJob.values()[0],
                UserType.INDIVIDUAL, null);
        AuthResponse second = auth.refresh(request(first.refreshToken()));
        assertThat(user.isProfileComplete()).isTrue();
        assertThat(user.isVerified()).isFalse();
        assertThat(user.getEmail()).isEqualTo("untrusted@example.test");
        assertThat(user.getPassword()).isNull();
        assertThat(user.getRefreshToken()).isEqualTo(hash(second.refreshToken()));
        rejectRefresh(first.refreshToken(), BUSINESS_ERROR.REFRESH_TOKEN_INVALID);
    }

    @Test
    void passwordAccountsStillRequireVerificationWithARealStoredRefresh() throws Exception {
        ReflectionTestUtils.setField(user, "isVerified", false);
        String refresh = issueRefresh();
        rejectRefresh(refresh, BUSINESS_ERROR.EMAIL_VERIFICATION_REQUIRED);
        assertThat(user.getRefreshToken()).isEqualTo(hash(refresh));
    }

    private String issueRefresh() throws Exception {
        String refresh = tokens.generateRefreshToken(user.getId());
        user.updateRefreshToken(hash(refresh));
        return refresh;
    }

    private void rejectRefresh(String token, BUSINESS_ERROR expected) {
        assertThatThrownBy(() -> auth.refresh(request(token))).isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode()).isEqualTo(expected));
    }

    private void rejectBearer(String token) throws Exception {
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized()).andExpect(header().doesNotExist("X-Token-Expired"));
        verifyNoInteractions(userDetailsService);
    }

    private void assertNormalAccess(String access) throws Exception {
        when(userDetailsService.loadUserById(user.getId())).thenReturn(CustomUserDetails.from(user));
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk());
    }

    private static RefreshRequest request(String token) {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken(token);
        return request;
    }

    private static String hash(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
