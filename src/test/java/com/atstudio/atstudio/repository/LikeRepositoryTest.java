package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.entity.Like;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.key.LikeId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("LikeRepository (복합 PK + @CreatedDate) 검증")
class LikeRepositoryTest {

    @Autowired private LikeRepository likeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TrackRepository trackRepository;

    private User savedUser;
    private Track savedTrack;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .nickname("user1")
                .email("user1@test.com")
                .build());

        savedTrack = trackRepository.save(Track.builder()
                .title("Track A")
                .bpm(100)
                .tonality("Am")
                .audioFile("a.mp3")
                .user(savedUser)
                .build());
    }

    @Test
    @DisplayName("Like 저장 후 복합 PK로 조회 성공")
    void save_and_findById() {
        LikeId id = new LikeId(savedUser.getId(), savedTrack.getId());
        likeRepository.save(Like.builder().id(id).user(savedUser).track(savedTrack).build());

        assertThat(likeRepository.findById(id)).isPresent();
    }

    @Test
    @DisplayName("@CreatedDate: createdAt이 자동으로 채워짐")
    void createdAt_isAutoPopulated() {
        LikeId id = new LikeId(savedUser.getId(), savedTrack.getId());
        likeRepository.save(Like.builder().id(id).user(savedUser).track(savedTrack).build());

        Like found = likeRepository.findById(id).orElseThrow();
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("같은 (user, track) 좋아요 중복 저장 시 레코드는 1개 유지 (JPA merge)")
    void duplicateLike_isIdempotent() {
        LikeId id = new LikeId(savedUser.getId(), savedTrack.getId());
        likeRepository.save(Like.builder().id(id).user(savedUser).track(savedTrack).build());
        likeRepository.save(Like.builder().id(id).user(savedUser).track(savedTrack).build());

        // 복합 PK 엔티티는 save()가 merge()를 호출 → 중복 INSERT 없음
        assertThat(likeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Like 삭제 후 조회 시 empty 반환")
    void delete_then_notFound() {
        LikeId id = new LikeId(savedUser.getId(), savedTrack.getId());
        likeRepository.save(Like.builder().id(id).user(savedUser).track(savedTrack).build());

        likeRepository.deleteById(id);

        assertThat(likeRepository.findById(id)).isEmpty();
    }
}
