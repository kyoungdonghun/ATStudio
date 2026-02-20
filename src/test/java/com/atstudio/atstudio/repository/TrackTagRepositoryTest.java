package com.atstudio.atstudio.repository;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackTag;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.entity.key.TrackTagId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("TrackTagRepository (복합 PK) 검증")
class TrackTagRepositoryTest {

    @Autowired private TrackTagRepository trackTagRepository;
    @Autowired private TrackRepository trackRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private UserRepository userRepository;

    private Track savedTrack;
    private Tag savedTag;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .nickname("artist")
                .email("artist@test.com")
                .build());

        savedTrack = trackRepository.save(Track.builder()
                .title("Test Track")
                .bpm(120)
                .tonality("C")
                .audioFile("test.mp3")
                .user(user)
                .build());

        savedTag = tagRepository.save(Tag.builder()
                .name("Pop")
                .type(TagType.GENRE)
                .build());
    }

    @Test
    @DisplayName("복합 PK로 저장 후 조회 성공")
    void save_and_findById_compositeKey() {
        TrackTagId id = new TrackTagId(savedTrack.getId(), savedTag.getId());
        TrackTag trackTag = TrackTag.builder()
                .id(id)
                .track(savedTrack)
                .tag(savedTag)
                .build();

        trackTagRepository.save(trackTag);

        assertThat(trackTagRepository.findById(id))
                .isPresent()
                .hasValueSatisfying(tt -> {
                    assertThat(tt.getTrack().getId()).isEqualTo(savedTrack.getId());
                    assertThat(tt.getTag().getId()).isEqualTo(savedTag.getId());
                });
    }

    @Test
    @DisplayName("복합 PK로 삭제 후 조회 시 empty 반환")
    void delete_by_compositeKey() {
        TrackTagId id = new TrackTagId(savedTrack.getId(), savedTag.getId());
        trackTagRepository.save(TrackTag.builder()
                .id(id).track(savedTrack).tag(savedTag).build());

        trackTagRepository.deleteById(id);

        assertThat(trackTagRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("동일 TrackTag 중복 저장 시 기존 레코드 유지 (upsert)")
    void save_duplicate_compositeKey_upserts() {
        TrackTagId id = new TrackTagId(savedTrack.getId(), savedTag.getId());
        trackTagRepository.save(TrackTag.builder().id(id).track(savedTrack).tag(savedTag).build());
        trackTagRepository.save(TrackTag.builder().id(id).track(savedTrack).tag(savedTag).build());

        assertThat(trackTagRepository.count()).isEqualTo(1);
    }
}
