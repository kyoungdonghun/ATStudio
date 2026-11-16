package com.atstudio.atstudio.security;

import com.atstudio.atstudio.controller.TagController;
import com.atstudio.atstudio.dto.tag.TagResponse;
import com.atstudio.atstudio.entity.enums.TagType;
import com.atstudio.atstudio.service.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real tag controller, method security and both listening admission modes; no external providers. */
class ListeningTagManagementTest {
    private final WebApplicationContextRunner base = new WebApplicationContextRunner()
            .withUserConfiguration(Fixture.class)
            .withPropertyValues("app.listening.enabled=true", "app.listening.operator-addresses[0]=192.0.2.10");
    private static final String BODY = "{\"name\":\"QA Tag\",\"type\":\"GENRE\"}";

    @Test void authenticatedAdminCanCreateReadAndEditWithRealController() {
        base.withPropertyValues("app.listening.operator-access=AUTHENTICATED_ADMIN").run(context -> {
            var service = context.getBean(TagService.class);
            var tag = new TagResponse(7L, "QA Tag", TagType.GENRE, null);
            when(service.createTag(any())).thenReturn(tag);
            when(service.updateTag(eq(7L), any())).thenReturn(tag);
            when(service.getAllTagsForAdmin(null)).thenReturn(List.of(tag));
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            for (String role : List.of("ANONYMOUS", "USER", "ADMIN")) {
                int denied = role.equals("USER") ? 403 : 401;
                for (var request : List.of(post("/api/tags"), put("/api/tags/7"), get("/api/tags/admin"))) {
                    request.header("X-ATStudio-Client-IP", "198.51.100.55").contentType("application/json").content(BODY);
                    if (!role.equals("ANONYMOUS")) request.with(user("fixture").roles(role));
                    int expected = role.equals("ADMIN") ? request.buildRequest(context.getServletContext()).getMethod().equals("POST") ? 201 : 200 : denied;
                    mvc.perform(request).andExpect(status().is(expected));
                }
            }
            verify(service, times(1)).createTag(argThat(request -> request.getName().equals("QA Tag") && request.getType() == TagType.GENRE));
            verify(service, times(1)).updateTag(eq(7L), any());
            verify(service, times(1)).getAllTagsForAdmin(null);
        });
    }

    @Test void defaultModeStillRequiresAllowedSourceForTagWritesAndPage() {
        base.run(context -> {
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            for (String source : List.of("198.51.100.55", "192.0.2.10")) {
                boolean allowed = source.equals("192.0.2.10");
                mvc.perform(post("/api/tags").header("X-ATStudio-Client-IP", source).with(user("admin").roles("ADMIN"))
                        .contentType("application/json").content(BODY)).andExpect(status().is(allowed ? 201 : 403));
                mvc.perform(put("/api/tags/7").header("X-ATStudio-Client-IP", source).with(user("admin").roles("ADMIN"))
                        .contentType("application/json").content(BODY)).andExpect(status().is(allowed ? 200 : 403));
                mvc.perform(get("/admin/tags").header("X-ATStudio-Client-IP", source))
                        .andExpect(status().is(allowed ? 200 : 403));
            }
            verify(context.getBean(TagService.class), times(1)).createTag(any());
            verify(context.getBean(TagService.class), times(1)).updateTag(eq(7L), any());
        });
    }

    @Test void deletionOtherMethodsAndUnrelatedAdminRemainDenied() {
        base.withPropertyValues("app.listening.operator-access=AUTHENTICATED_ADMIN").run(context -> {
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            for (var request : List.of(delete("/api/tags/7"), patch("/api/tags/7"), put("/api/tags"),
                    post("/api/tags/admin"), post("/api/tags/available"), get("/api/tags/7/deletion-impact"),
                    get("/admin/users"), get("/api/payments"), get("/api/tracks/7/download"))) {
                mvc.perform(request.header("X-ATStudio-Client-IP", "198.51.100.55").with(user("admin").roles("ADMIN")))
                        .andExpect(status().isForbidden());
            }
            verify(context.getBean(TagService.class), never()).deleteTag(anyLong());
        });
    }

    @Test void nonCanonicalTagWritesNeverReachService() {
        base.withPropertyValues("app.listening.operator-access=AUTHENTICATED_ADMIN").run(context -> {
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            for (String path : List.of("/api/tags/0", "/api/tags/07", "/api/tags/7/", "/api/tags/7/extra", "/api/tags/admin")) {
                mvc.perform(put(URI.create(path)).header("X-ATStudio-Client-IP", "198.51.100.55")
                        .with(user("admin").roles("ADMIN")).contentType("application/json").content(BODY))
                        .andExpect(status().isForbidden());
            }
            verify(context.getBean(TagService.class), never()).updateTag(anyLong(), any());
        });
    }

    @Test void publicTagReadStillUsesPublicServiceAndIgnoresStaleToken() {
        base.withPropertyValues("app.listening.operator-access=AUTHENTICATED_ADMIN").run(context -> {
            var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
            clearInvocations(context.getBean(JwtTokenProvider.class));
            mvc.perform(get("/api/tags").header("Authorization", "Bearer stale")).andExpect(status().isOk());
            verify(context.getBean(TagService.class)).getAllTags(null);
            verify(context.getBean(TagService.class), never()).getAllTagsForAdmin(any());
            verifyNoInteractions(context.getBean(JwtTokenProvider.class));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Import({ListeningAuthenticatedAdminTest.Fixture.class, TagController.class})
    static class Fixture {
        @Bean TagService tagService() { return mock(TagService.class); }
    }
}
