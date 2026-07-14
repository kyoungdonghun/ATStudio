package com.atstudio.atstudio.controller;

import com.atstudio.atstudio.entity.enums.UserRole;
import com.atstudio.atstudio.security.CustomUserDetails;
import com.atstudio.atstudio.service.EmailService;
import com.atstudio.atstudio.service.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DisplayName("AuthController logout 계약 테스트")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AuthService authService;
    @MockitoBean EmailService emailService;

    @Test
    @DisplayName("POST /api/auth/logout - 비인증 요청은 401")
    void logout_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /api/auth/logout - 인증 요청은 body 없는 204이며 반복 호출 가능")
    void logout_authenticated_isBodylessNoContentAndIdempotent() throws Exception {
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(1L)
                .email("user@test.com")
                .password("encoded")
                .role(UserRole.USER)
                .isDeleted(false)
                .isProfileComplete(true)
                .build();

        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(post("/api/auth/logout").with(user(userDetails)))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));
        }

        verify(authService, times(2)).logout(1L);
    }
}
