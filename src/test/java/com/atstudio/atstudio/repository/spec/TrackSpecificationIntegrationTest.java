package com.atstudio.atstudio.repository.spec;

import com.atstudio.atstudio.config.JpaConfig;
import com.atstudio.atstudio.entity.Tag;
import com.atstudio.atstudio.entity.Track;
import com.atstudio.atstudio.entity.TrackTag;
import com.atstudio.atstudio.entity.User;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.entity.key.TrackTagId;
import com.atstudio.atstudio.repository.TagRepository;
import com.atstudio.atstudio.repository.TrackRepository;
import com.atstudio.atstudio.repository.TrackTagRepository;
import com.atstudio.atstudio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("TrackSpecification search contract")
class TrackSpecificationIntegrationTest {

    @Autowired TrackRepository trackRepository;
    @Autowired TrackTagRepository trackTagRepository;
    @Autowired TagRepository tagRepository;
    @Autowired UserRepository userRepository;

    private Track springTrack;
    private Track nightTrack;

    @BeforeEach
    void setUp() {
        User creator = userRepository.save(User.builder()
                .email("spec-creator@example.com")
                .nickname("spec-creator")
                .build());
        springTrack = trackRepository.save(Track.builder()
                .title("Spring Morning")
                .bpm(92)
                .tonality("C")
                .audioFile("tracks/audio/spring.wav")
                .user(creator)
                .isActive(true)
                .build());
        nightTrack = trackRepository.save(Track.builder()
                .title("Night Drive")
                .bpm(132)
                .tonality("Am")
                .audioFile("tracks/audio/night.wav")
                .user(creator)
                .isActive(false)
                .build());

        Tag shorts = tagRepository.save(Tag.builder().name("Shorts").type(TagType.USAGE).build());
        Tag vlog = tagRepository.save(Tag.builder().name("Vlog").type(TagType.USAGE).build());
        Tag springGenre = tagRepository.save(Tag.builder().name("SpringOnlyGenre").type(TagType.GENRE).build());
        attach(springTrack, shorts);
        attach(springTrack, vlog);
        attach(nightTrack, springGenre);
    }

    @Test
    @DisplayName("keyword search matches normalized title or USAGE tag, but not another tag type")
    void keywordContains_matchesTitleAndUsageOnly() {
        assertThat(trackRepository.findAll(TrackSpecification.keywordContains("spring")))
                .extracting(Track::getId)
                .containsExactly(springTrack.getId());
        assertThat(trackRepository.findAll(TrackSpecification.keywordContains("SHORTS")))
                .extracting(Track::getId)
                .containsExactly(springTrack.getId());
        assertThat(trackRepository.findAll(TrackSpecification.keywordContains("SpringOnlyGenre")))
                .isEmpty();
    }

    @Test
    @DisplayName("title, activity, BPM, and tonality predicates compose without widening results")
    void scalarPredicates_composeAsAndContract() {
        Specification<Track> matching = Specification
                .where(TrackSpecification.hasIsActive(true))
                .and(TrackSpecification.titleContains("MORNING"))
                .and(TrackSpecification.hasBpmMin(90))
                .and(TrackSpecification.hasBpmMax(100))
                .and(TrackSpecification.hasTonality("C"));

        assertThat(trackRepository.findAll(matching))
                .extracting(Track::getId)
                .containsExactly(springTrack.getId());
        assertThat(trackRepository.findAll(TrackSpecification.isActive()))
                .extracting(Track::getId)
                .containsExactly(springTrack.getId());
        assertThat(trackRepository.findAll(TrackSpecification.hasIsActive(null)))
                .extracting(Track::getId)
                .containsExactlyInAnyOrder(springTrack.getId(), nightTrack.getId());
    }

    @Test
    @DisplayName("multi-tag search requires every requested tag of the requested type")
    void hasAllTagsWithType_requiresAllTags() {
        assertThat(trackRepository.findAll(
                TrackSpecification.hasTagWithNameAndType("Shorts", "USAGE")))
                .extracting(Track::getId)
                .containsExactly(springTrack.getId());
        assertThat(trackRepository.findAll(
                TrackSpecification.hasAllTagsWithType(List.of("Shorts", "Vlog"), "USAGE")))
                .extracting(Track::getId)
                .containsExactly(springTrack.getId());
        assertThat(trackRepository.findAll(
                TrackSpecification.hasAllTagsWithType(List.of("Shorts", "Missing"), "USAGE")))
                .isEmpty();
    }

    @Test
    @DisplayName("empty optional filters are absent rather than producing accidental predicates")
    void emptyFilters_areAbsent() {
        assertThat(TrackSpecification.titleContains(null)).isNull();
        assertThat(TrackSpecification.titleContains("  ")).isNull();
        assertThat(TrackSpecification.keywordContains(null)).isNull();
        assertThat(TrackSpecification.keywordContains(" ")).isNull();
        assertThat(TrackSpecification.hasBpmMin(null)).isNull();
        assertThat(TrackSpecification.hasBpmMax(null)).isNull();
        assertThat(TrackSpecification.hasTonality(null)).isNull();
        assertThat(TrackSpecification.hasTonality(" ")).isNull();
        assertThat(TrackSpecification.hasTagWithNameAndType(null, "USAGE")).isNull();
        assertThat(TrackSpecification.hasTagWithNameAndType(" ", "USAGE")).isNull();
        assertThat(TrackSpecification.hasAllTagsWithType(null, "USAGE")).isNull();
        assertThat(TrackSpecification.hasAllTagsWithType(List.of(), "USAGE")).isNull();
    }

    private void attach(Track track, Tag tag) {
        trackTagRepository.save(TrackTag.builder()
                .id(new TrackTagId(track.getId(), tag.getId()))
                .track(track)
                .tag(tag)
                .build());
    }
}
