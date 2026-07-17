package com.atstudio.atstudio.security;

import com.atstudio.atstudio.entity.enums.UserRole;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void nonBearerAuthorizationIsIgnored() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic credentials");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken("Basic credentials");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void validBearerTokenAuthenticatesResolvedUser() throws Exception {
        MockHttpServletRequest request = bearerRequest("valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(7L)
                .email("user@example.com")
                .password("encoded-password")
                .role(UserRole.USER)
                .build();
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(TokenValidationResult.VALID);
        when(jwtTokenProvider.getUserID("valid-token")).thenReturn(7L);
        when(userDetailsService.loadUserById(7L)).thenReturn(userDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull()
                .extracting(authentication -> authentication.getPrincipal())
                .isSameAs(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void expiredBearerTokenClearsAuthenticationAndSignalsExpiry() throws Exception {
        MockHttpServletRequest request = bearerRequest("expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("stale", null));
        when(jwtTokenProvider.validateToken("expired-token")).thenReturn(TokenValidationResult.EXPIRED);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getHeader("X-Token-Expired")).isEqualTo("true");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidBearerTokenClearsAuthenticationWithoutExpirySignal() throws Exception {
        MockHttpServletRequest request = bearerRequest("invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("stale", null));
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(TokenValidationResult.INVALID);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getHeader("X-Token-Expired")).isNull();
        verify(filterChain).doFilter(request, response);
    }

    private MockHttpServletRequest bearerRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
