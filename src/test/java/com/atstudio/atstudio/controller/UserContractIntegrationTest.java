package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.entity.enums.UserType;
import com.atstudio.atstudio.repository.DownloadQueueRepository;
import com.atstudio.atstudio.repository.LicenseRepository;
import com.atstudio.atstudio.repository.LikeRepository;
import com.atstudio.atstudio.repository.PlayHistoryRepository;
import com.atstudio.atstudio.repository.TrackDownloadRepository;
import com.atstudio.atstudio.repository.UserRepository;
import com.atstudio.atstudio.repository.WhitelistChannelRepository;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.security.CustomUserDetailsService;
import com.atstudio.atstudio.security.JwtTokenProvider;
import com.atstudio.atstudio.service.EmailService;
import com.atstudio.atstudio.service.auth.PasswordLoginPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("User API 계약 통합 테스트")
class UserContractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @MockitoBean UserRepository userRepository;
    @MockitoBean PasswordEncoder passwordEncoder;
    @MockitoBean EmailService emailService;
    @MockitoBean LikeRepository likeRepository;
    @MockitoBean DownloadQueueRepository downloadQueueRepository;
    @MockitoBean PlayHistoryRepository playHistoryRepository;
    @MockitoBean TrackDownloadRepository trackDownloadRepository;
    @MockitoBean LicenseRepository licenseRepository;
    @MockitoBean WhitelistChannelRepository whitelistChannelRepository;
    @MockitoBean PasswordLoginPolicy passwordLoginPolicy;
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/users - 연락처 누락 → 400 INVALID_VALID")
    void register_missingPhone_returns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "creator01",
                                  "email": "user@example.com",
                                  "password": "SecureP@ss123",
                                  "phonePersonal": null,
                                  "job": "EDITOR",
                                  "userType": "INDIVIDUAL"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_VALID"));

        verifyNoInteractions(userRepository, passwordEncoder, emailService);
    }

    @Test
    @DisplayName("POST /api/users - 기업 회원 회사명 누락 → 400 INVALID_VALID")
    void register_businessMissingCompanyName_returns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "bizcreator",
                                  "email": "biz@example.com",
                                  "password": "SecureP@ss123",
                                  "phonePersonal": "010-1234-5678",
                                  "job": null,
                                  "companyName": null,
                                  "userType": "BUSINESS"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_VALID"));

        verifyNoInteractions(userRepository, passwordEncoder, emailService);
    }

    @Test
    @DisplayName("PUT /api/users/me/complete-profile - 개인 회원 직업 누락 → 400 INVALID_VALID")
    void completeProfile_individualMissingJob_returns400() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(buildUserDetails(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildIncompleteUser(1L)));

        mockMvc.perform(put("/api/users/me/complete-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "creator01",
                                  "phonePersonal": "010-1234-5678",
                                  "job": null,
                                  "userType": "INDIVIDUAL"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_VALID"));
    }

    @Test
    @DisplayName("PUT /api/users/me/complete-profile - 기업 회원 회사명 누락 → 400 INVALID_VALID")
    void completeProfile_businessMissingCompanyName_returns400() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(buildUserDetails(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildIncompleteUser(1L)));

        mockMvc.perform(put("/api/users/me/complete-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "bizcreator",
                                  "phonePersonal": "010-1234-5678",
                                  "job": null,
                                  "companyName": "   ",
                                  "userType": "BUSINESS"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_VALID"));
    }

    @Test
    @DisplayName("PUT /api/users/me/complete-profile - 기업 회원 회사명 제공 시 200 OK")
    void completeProfile_businessWithCompanyName_returns200() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(buildUserDetails(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildIncompleteUser(1L)));
        when(userRepository.findByNickname("bizcreator")).thenReturn(Optional.empty());
        when(userRepository.findByPhonePersonal("010-1234-5678")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/me/complete-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "bizcreator",
                                  "phonePersonal": "010-1234-5678",
                                  "job": null,
                                  "companyName": "ATStudio Biz",
                                  "userType": "BUSINESS"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userType").value("BUSINESS"))
                .andExpect(jsonPath("$.data.companyName").value("ATStudio Biz"))
                .andExpect(jsonPath("$.data.job").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/users/me - 기업 회원 회사명 빈값 → 400 INVALID_ARGUMENT")
    void updateMyProfile_businessBlankCompanyName_returns400() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(buildUserDetails(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildBusinessUser(1L)));

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "bizcreator",
                                  "phonePersonal": "010-1234-5678",
                                  "companyName": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"));
    }

    @Test
    @DisplayName("PUT /api/users/me - 기업 회원이 회사명을 생략하면 기존 값 유지")
    void updateMyProfile_businessOmittedCompanyName_preservesExistingValue() throws Exception {
        String token = jwtTokenProvider.generateAccessToken(1L, UserRole.USER);
        when(customUserDetailsService.loadUserById(1L)).thenReturn(buildUserDetails(1L));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildBusinessUser(1L)));
        when(userRepository.findByNickname("bizcreator2")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "bizcreator2",
                                  "phonePersonal": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("ATStudio Biz"))
                .andExpect(jsonPath("$.data.userType").value("BUSINESS"));
    }

    private CustomUserDetails buildUserDetails(Long id) {
        return CustomUserDetails.builder()
                .id(id)
                .email("user@test.com")
                .password("encoded")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(false)
                .build();
    }

    private User buildIncompleteUser(Long id) {
        User user = User.builder()
                .email("social@test.com")
                .nickname("socialNick")
                .password(null)
                .role(UserRole.USER)
                .userType(UserType.INDIVIDUAL)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private User buildBusinessUser(Long id) {
        User user = User.builder()
                .email("biz@test.com")
                .nickname("bizcreator")
                .password("encoded")
                .phonePersonal("010-1234-5678")
                .companyName("ATStudio Biz")
                .role(UserRole.USER)
                .userType(UserType.BUSINESS)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
