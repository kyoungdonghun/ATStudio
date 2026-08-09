package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.entity.enums.PaymentProviderType;
import com.atstudio.atstudio.entity.enums.UserRole;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

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
    @DisplayName("refresh session 변경용 사용자 조회는 비관적 쓰기 잠금을 사용")
    void findByIdForUpdate_usesPessimisticWriteLock() throws Exception {
        User saved = userRepository.saveAndFlush(buildUser("locked", "locked@test.com"));

        assertThat(userRepository.findByIdForUpdate(saved.getId()))
                .contains(saved);
        assertThat(UserRepository.class.getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class)
                .value())
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    @DisplayName("역할 변경 guard는 삭제되지 않은 ADMIN만 ID 순서로 잠금")
    void findActiveAdminsForRoleChange_excludesDeletedAndOrdersById() throws Exception {
        User laterAdmin = buildUser("later-admin", "later-admin@test.com");
        laterAdmin.updateByAdmin(UserRole.ADMIN, null);
        User deletedAdmin = buildUser("deleted-admin", "deleted-admin@test.com");
        deletedAdmin.updateByAdmin(UserRole.ADMIN, null);
        deletedAdmin.withdraw();
        User earlierAdmin = buildUser("earlier-admin", "earlier-admin@test.com");
        earlierAdmin.updateByAdmin(UserRole.ADMIN, null);
        userRepository.saveAllAndFlush(java.util.List.of(laterAdmin, deletedAdmin, earlierAdmin));

        assertThat(userRepository.findActiveAdminsForRoleChange())
                .extracting(User::getId)
                .containsExactly(laterAdmin.getId(), earlierAdmin.getId());
        assertThat(UserRepository.class.getMethod("findActiveAdminsForRoleChange")
                .getAnnotation(Lock.class)
                .value())
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    @DisplayName("withdrawal payment locks do not fetch-lock the user before the ADMIN guard")
    void withdrawalPreGuardLocks_doNotFetchJoinUser() throws Exception {
        var agreementMethod = BillingAgreementRepository.class.getMethod(
                "findByUserIDAndProviderForUpdate",
                Long.class,
                PaymentProviderType.class);
        var subscriptionMethod = UserSubscriptionRepository.class.getMethod(
                "findByUserIDForUpdate",
                Long.class);

        assertThat(agreementMethod.getAnnotation(Lock.class).value())
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(agreementMethod.getAnnotation(Query.class).value())
                .doesNotContainIgnoringCase("JOIN");
        assertThat(subscriptionMethod.getAnnotation(Lock.class).value())
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(subscriptionMethod.getAnnotation(Query.class).value())
                .doesNotContainIgnoringCase("JOIN FETCH us.user");
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
