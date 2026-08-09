package com.atstudio.atstudio.service;

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
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import({JpaConfig.class, TagService.class})
@DisplayName("TagService available-tag query contract")
class TagServiceAvailableTagsIntegrationTest {

    @Autowired private TagService tagService;
    @Autowired private TagRepository tagRepository;
    @Autowired private TrackRepository trackRepository;
    @Autowired private TrackTagRepository trackTagRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private EntityManagerFactory entityManagerFactory;

    private Tag rock;
    private Tag pianoSynth;
    private Tag kit808;

    @BeforeEach
    void setUp() {
        User creator = userRepository.save(User.builder()
                .email("available-tags@example.com")
                .nickname("available-tags")
                .build());

        rock = saveTag("K-Pop", TagType.GENRE);
        pianoSynth = saveTag("Piano, Synth", TagType.INSTRUMENT);
        kit808 = saveTag("808 #Kit", TagType.INSTRUMENT);
        Tag usage = saveTag("쇼츠 용", TagType.USAGE);
        Tag exactOnly = saveTag("Exact Match", TagType.MOOD);
        Tag partialOnly = saveTag("Partial Match", TagType.MOOD);
        Tag inactiveOnly = saveTag("Inactive Match", TagType.MOOD);

        Track exact = saveTrack(creator, "Exact", true);
        attach(exact, rock, pianoSynth, kit808, usage, exactOnly);

        Track partial = saveTrack(creator, "Partial", true);
        attach(partial, rock, pianoSynth, usage, partialOnly);

        Track inactive = saveTrack(creator, "Inactive", false);
        attach(inactive, rock, pianoSynth, kit808, usage, inactiveOnly);

        entityManager.flush();
        entityManager.clear();
        statistics().clear();
    }

    @Test
    @DisplayName("Instrument values are bound atomically, AND-composed, active-only, and loaded in one query")
    void instrumentFilters_preserveSpecialCharactersAndAndSemantics() {
        var result = tagService.getAvailableTags(
                List.of("K-Pop"),
                null,
                List.of("Piano, Synth", "808 #Kit"),
                List.of("쇼츠 용"),
                null,
                null);

        assertThat(result)
                .extracting(response -> response.name())
                .contains("K-Pop", "Piano, Synth", "808 #Kit", "쇼츠 용", "Exact Match")
                .doesNotContain("Partial Match", "Inactive Match");
        assertThat(statistics().getPrepareStatementCount()).isEqualTo(1L);
    }

    private Track saveTrack(User creator, String title, boolean active) {
        return trackRepository.save(Track.builder()
                .title(title)
                .bpm(100)
                .tonality("C")
                .audioFile("tracks/audio/" + title.toLowerCase() + ".wav")
                .user(creator)
                .isActive(active)
                .build());
    }

    private Tag saveTag(String name, TagType type) {
        return tagRepository.save(Tag.builder().name(name).type(type).build());
    }

    private void attach(Track track, Tag... tags) {
        for (Tag tag : tags) {
            trackTagRepository.save(TrackTag.builder()
                    .id(new TrackTagId(track.getId(), tag.getId()))
                    .track(track)
                    .tag(tag)
                    .build());
        }
    }

    private org.hibernate.stat.Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }
}
