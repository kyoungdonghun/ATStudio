package com.atstudio.atstudio.security;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadByUsernameReturnsActiveUserDetails() {
        User user = user(11L, "active@example.com");
        when(userRepository.findByEmail("active@example.com")).thenReturn(Optional.of(user));

        CustomUserDetails result = (CustomUserDetails) userDetailsService
                .loadUserByUsername("active@example.com");

        assertThat(result.getId()).isEqualTo(11L);
        assertThat(result.getUsername()).isEqualTo("active@example.com");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void loadByUsernameRejectsUnknownUser() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void loadByUsernameRejectsDeactivatedUser() {
        User user = user(12L, "deleted@example.com");
        user.withdraw();
        when(userRepository.findByEmail("deleted@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("deleted@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User account is deactivated");
    }

    @Test
    void loadByIdReturnsActiveUserDetails() {
        User user = user(21L, "by-id@example.com");
        when(userRepository.findById(21L)).thenReturn(Optional.of(user));

        CustomUserDetails result = (CustomUserDetails) userDetailsService.loadUserById(21L);

        assertThat(result.getId()).isEqualTo(21L);
        assertThat(result.getUsername()).isEqualTo("by-id@example.com");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void loadByIdRejectsUnknownUser() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserById(404L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void loadByIdRejectsDeactivatedUser() {
        User user = user(22L, "deleted-by-id@example.com");
        user.withdraw();
        when(userRepository.findById(22L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userDetailsService.loadUserById(22L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User account is deactivated");
    }

    private User user(Long id, String email) {
        User user = User.builder()
                .email(email)
                .nickname("security-test-user")
                .password("encoded-password")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
