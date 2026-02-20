package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("UserRepository 검증")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User buildUser(String nickname, String email) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .build();
    }

    @Test
    @DisplayName("저장 후 ID로 조회 성공")
    void save_and_findById() {
        User saved = userRepository.save(buildUser("alice", "alice@test.com"));

        assertThat(userRepository.findById(saved.getId()))
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getNickname()).isEqualTo("alice");
                    assertThat(u.getEmail()).isEqualTo("alice@test.com");
                });
    }

    @Test
    @DisplayName("중복 email 저장 시 DataIntegrityViolationException 발생")
    void duplicateEmail_throwsException() {
        userRepository.save(buildUser("alice", "dup@test.com"));
        userRepository.flush();

        assertThatThrownBy(() -> {
            userRepository.save(buildUser("bob", "dup@test.com"));
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("중복 nickname 저장 시 DataIntegrityViolationException 발생")
    void duplicateNickname_throwsException() {
        userRepository.save(buildUser("dupNick", "a@test.com"));
        userRepository.flush();

        assertThatThrownBy(() -> {
            userRepository.save(buildUser("dupNick", "b@test.com"));
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("삭제 후 조회 시 empty 반환")
    void delete_then_notFound() {
        User saved = userRepository.save(buildUser("alice", "alice@test.com"));
        userRepository.delete(saved);

        assertThat(userRepository.findById(saved.getId())).isEmpty();
    }
}
