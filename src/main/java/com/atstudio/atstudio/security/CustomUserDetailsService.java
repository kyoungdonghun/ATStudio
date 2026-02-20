package com.atstudio.atstudio.security;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.isDeleted()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }
        return CustomUserDetails.from(user);
    }

    public UserDetails loadUserById(Long userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.isDeleted()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }
        return CustomUserDetails.from(user);
    }
}
