package com.atstudio.atstudio.security;

import com.atstudio.atstudio.entity.*;
import com.atstudio.atstudio.entity.enums.*;
import com.atstudio.atstudio.repository.*;
import com.atstudio.atstudio.service.ReservedSettingPolicy;
import com.atstudio.atstudio.testfixture.SyntheticAudioFixtures;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real login, JWT, request filters, JPA rows and file responses in one isolated application. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pilotWorkflow", "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.listening.enabled=true", "app.listening.catalog-source=DATABASE",
        "app.listening.operator-access=AUTHENTICATED_ADMIN", "app.listening.pilot-enabled=true",
        "app.listening.pilot-user-ids=1,2", "app.security.trusted-client-identity.enabled=true",
        "app.bootstrap.test-users.enabled=false", "app.background-tasks.enabled=false",
        "app.audio.worker-enabled=false", "app.storage.capture-control-enabled=false"
})
@AutoConfigureMockMvc
class ListeningPilotWorkflowIntegrationTest {
    @TempDir static Path temp;
    @DynamicPropertySource
    static void storage(DynamicPropertyRegistry registry) {
        registry.add("app.storage.public-path", () -> temp.resolve("public").toString());
        registry.add("app.storage.private-path", () -> temp.resolve("private").toString());
    }

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserRepository users;
    @Autowired SubscriptionRepository plans;
    @Autowired UserSubscriptionRepository grants;
    @Autowired TrackRepository tracks;
    @Autowired SiteSettingRepository settings;
    @Autowired TrackDownloadRepository history;
    @Autowired LicenseRepository licenses;

    private final String password = UUID.randomUUID().toString();
    private final LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

    @Test void twoRealUsersReceiveOnlyTheirOwnFilesAndRecordsWithinThePilot() throws Exception {
        User alice = account("pilot-a");
        User bob = account("pilot-b");
        assertThat(alice.getId()).isEqualTo(1L);
        assertThat(bob.getId()).isEqualTo(2L);
        Subscription plan = plans.save(Subscription.builder().name("Local daily one")
                .userType(UserType.INDIVIDUAL).priceMonthly(BigDecimal.ZERO).priceYearly(BigDecimal.ZERO)
                .downloadPerDay(1).build());
        grant(alice, plan);
        UserSubscription bobGrant = grant(bob, plan);
        byte[] firstAudio = SyntheticAudioFixtures.mp3Cbr(128, 6);
        byte[] secondAudio = SyntheticAudioFixtures.mp3Cbr(96, 8);
        User uploader = users.saveAndFlush(User.builder().nickname("operator").email("operator@example.invalid")
                .password(encoder.encode(password)).role(UserRole.ADMIN).isVerified(true).build());
        Track first = track(uploader, "first", firstAudio);
        Track second = track(uploader, "second", secondAudio);
        settings.save(new SiteSetting(ReservedSettingPolicy.CATALOG_KEY, "{\"revision\":0,\"trackIds\":[]}"));
        String admin = login(uploader);
        String a = login(alice);
        String b = login(bob);
        String firstFile = "/api/tracks/" + first.getId() + "/download";
        String secondFile = "/api/tracks/" + second.getId() + "/download";
        getAs(firstFile, a).andExpect(status().isNotFound());
        assertThat(history.count()).isZero();
        assertThat(licenses.count()).isZero();
        publish(admin, 0, List.of(first.getId(), second.getId()));
        simultaneousFirstDownloads(firstFile, a, firstAudio);
        assertThat(history.count()).isEqualTo(1);
        assertThat(licenses.count()).isEqualTo(1);
        getAs("/api/downloads/history", b).andExpect(status().isOk()).andExpect(jsonPath("$.dataList").isEmpty());
        getAs("/api/licenses/me", b).andExpect(status().isOk()).andExpect(jsonPath("$.dataList").isEmpty());
        getAs(secondFile, b).andExpect(status().isOk()).andExpect(content().bytes(secondAudio));
        String personal = "/api/licenses/" + licenseId(a);
        String record = getAs(personal, a).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-store")))
                .andExpect(jsonPath("$.data.user.id").value(alice.getId()))
                .andReturn().getResponse().getContentAsString();
        String code = JsonPath.read(record, "$.data.licenseCode");
        assertThat(UUID.fromString(code).toString()).isEqualTo(code);
        getAs(personal, b).andExpect(status().isNotFound());
        getAs("/api/licenses/" + licenseId(b), a).andExpect(status().isNotFound());
        ownHistory(a, first);
        ownHistory(b, second);

        getAs(firstFile, a).andExpect(status().isOk()).andExpect(content().bytes(firstAudio))
                .andExpect(header().string("Cache-Control", containsString("no-store")));
        getAs(secondFile, a).andExpect(status().isForbidden());
        getAs(personal, a).andExpect(jsonPath("$.data.licenseCode").value(code));
        assertThat(history.count()).isEqualTo(2);
        assertThat(licenses.count()).isEqualTo(2);
        getAs("/api/utils/download-count", a).andExpect(status().isOk()).andExpect(jsonPath("$.data.remaining").value(0));
        mvc.perform(get(firstFile).header("X-ATStudio-Client-IP", "192.0.2.55"))
                .andExpect(status().is4xxClientError());
        getAs("/api/downloads/history/track-ids", a).andExpect(status().isForbidden());
        getAs("/api/tracks/admin", a).andExpect(status().isForbidden());

        publish(admin, 1, List.of(second.getId()));
        String draftTitle = "Unpublished editorial title";
        Track draft = tracks.findById(first.getId()).orElseThrow();
        assertThat(draft.getDownloadCount()).isEqualTo(1);
        draft.update(draftTitle, null, null, null);
        tracks.saveAndFlush(draft);
        getAs(firstFile, a).andExpect(status().is4xxClientError());
        for (String path : new String[] {"/api/downloads/history", "/api/licenses/me", personal}) {
            String ledger = getAs(path, a).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(ledger).doesNotContain(draftTitle);
        }
        getAs("/api/downloads/history?keyword=editorial", a).andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList").isEmpty());
        getAs(personal, a).andExpect(jsonPath("$.data.licenseCode").value(code));
        publish(admin, 2, List.of(first.getId(), second.getId()));
        getAs(firstFile, a).andExpect(status().isOk()).andExpect(content().bytes(firstAudio));
        getAs(personal, a).andExpect(jsonPath("$.data.licenseCode").value(code))
                .andExpect(jsonPath("$.data.track.title").value(draftTitle));
        getAs("/api/utils/download-count", a).andExpect(jsonPath("$.data.remaining").value(0));
        bobGrant.adminUpdate(SubscriptionStatus.EXPIRED, null, today.minusDays(1));
        grants.saveAndFlush(bobGrant);
        getAs(secondFile, b).andExpect(status().is4xxClientError());
        getAs("/api/downloads/history", b).andExpect(status().isForbidden());
        getAs("/api/licenses/me", b).andExpect(status().isForbidden());
        assertThat(history.count()).isEqualTo(2);
        assertThat(licenses.count()).isEqualTo(2);
        User withdrawn = users.findById(alice.getId()).orElseThrow();
        withdrawn.withdraw();
        users.saveAndFlush(withdrawn);
        getAs("/api/downloads/history", a).andExpect(status().isUnauthorized());
        getAs(personal, a).andExpect(status().isUnauthorized());
        getAs(firstFile, a).andExpect(status().isUnauthorized());
    }

    private void publish(String adminToken, long revision, List<Long> selected) throws Exception {
        String body = """
                {"expectedRevision":%d,"trackIds":%s,"reason":"Local pilot review",
                 "creditsConfirmed":true,"attemptId":"%s"}
                """.formatted(revision, selected, UUID.randomUUID());
        mvc.perform(put("/api/tracks/admin/listening-catalog")
                        .header("Authorization", "Bearer " + adminToken)
                        .header("X-ATStudio-Client-IP", "192.0.2.55")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.revision").value((int) revision + 1));
    }

    private void ownHistory(String token, Track track) throws Exception {
        getAs("/api/downloads/history", token).andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList.length()").value(1))
                .andExpect(jsonPath("$.dataList[0].trackId").value(track.getId()));
    }

    private void simultaneousFirstDownloads(String path, String token, byte[] expected) throws Exception {
        var workers = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        Callable<Void> click = () -> {
            ready.countDown();
            assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
            getAs(path, token).andExpect(status().isOk()).andExpect(content().bytes(expected));
            return null;
        };
        try {
            var first = workers.submit(click);
            var second = workers.submit(click);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            first.get(20, TimeUnit.SECONDS);
            second.get(20, TimeUnit.SECONDS);
        } finally {
            start.countDown();
            workers.shutdownNow();
        }
    }

    private int licenseId(String token) throws Exception {
        String records = getAs("/api/licenses/me", token).andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList.length()").value(1))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(records, "$.dataList[0].id");
    }

    private User account(String name) {
        return users.saveAndFlush(User.builder().nickname(name).email(name + "@example.invalid")
                .password(encoder.encode(password)).isVerified(true).build());
    }

    private UserSubscription grant(User user, Subscription plan) {
        return grants.saveAndFlush(UserSubscription.builder().user(user).subscription(plan)
                .status(SubscriptionStatus.CANCELLED).billingCycle(BillingCycle.MONTHLY)
                .startedAt(today.minusDays(1)).expiresAt(today.plusDays(1)).build());
    }

    private Track track(User owner, String name, byte[] bytes) throws Exception {
        String key = "tracks/audio/" + name + ".mp3";
        Path file = temp.resolve("public").resolve(key);
        Files.createDirectories(file.getParent());
        Files.write(file, bytes);
        return tracks.saveAndFlush(Track.builder().title(name).tonality("C").bpm(100)
                .audioFile(key).isActive(true).user(owner).build());
    }

    private String login(User account) throws Exception {
        String body = "{\"email\":\"" + account.getEmail() + "\",\"password\":\"" + password + "\"}";
        String result = mvc.perform(post("/api/auth/login").header("X-ATStudio-Client-IP", "192.0.2.55")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(result, "$.data.accessToken");
    }

    private ResultActions getAs(String path, String token) throws Exception {
        return mvc.perform(get(path).header("Authorization", "Bearer " + token)
                .header("X-ATStudio-Client-IP", "192.0.2.55"));
    }
}
