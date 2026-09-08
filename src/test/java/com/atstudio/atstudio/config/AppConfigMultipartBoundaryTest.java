package com.atstudio.atstudio.config;

import com.atstudio.atstudio.common.dto.ExceptionResponseDTO;
import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.GlobalExceptionHandler;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.http.Part;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.connector.Request;
import org.apache.coyote.InputBuffer;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.http.InvalidParameterException;
import org.apache.tomcat.util.http.fileupload.impl.FileCountLimitExceededException;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.atstudio.atstudio.common.validation.ValidationConstants.ATTACHMENT_MAX_COUNT;
import static com.atstudio.atstudio.common.validation.ValidationConstants.CERT_DOC_MAX_COUNT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("OPS-01: real Tomcat multipart parsing without a running server")
class AppConfigMultipartBoundaryTest {

    private static final String BOUNDARY = "ATS005SyntheticMultipartBoundary";
    private static final int MAX_PARTS = 128;
    private static final int TRACK_EDIT_FIXED_PARTS = 8;

    @TempDir
    Path uploadDirectory;

    @Test
    void customizerBoundsPartsWithoutWeakeningOtherConnectorLimits() {
        Connector defaults = new Connector();
        Connector configured = configuredConnector();

        assertThat(configured.getMaxPartCount()).isEqualTo(MAX_PARTS);
        assertThat(configured.getMaxParameterCount()).isEqualTo(defaults.getMaxParameterCount());
        assertThat(configured.getMaxPostSize()).isEqualTo(defaults.getMaxPostSize());
        assertThat(configured.getMaxPartHeaderSize()).isEqualTo(defaults.getMaxPartHeaderSize());
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void acceptsTrackFormsWithIndividuallyEncodedTags(String method) throws Exception {
        List<FormPart> form = trackForm(method, 12);
        Request request = request(configuredConnector(), method, form);

        try {
            assertThat(request.getParts()).hasSize("PUT".equals(method) ? 20 : 18);
            assertThat(request.getParameterValues("tagIds"))
                    .containsExactly(IntStream.rangeClosed(1, 12).mapToObj(Integer::toString).toArray(String[]::new));
            assertThat(request.getPart("audioFile").getSubmittedFileName()).isEqualTo("synthetic.wav");
            assertThat(request.getPart("thumbnail").getSubmittedFileName()).isEqualTo("synthetic.png");
            if ("PUT".equals(method)) {
                assertThat(request.getParameter("replaceTags")).isEqualTo("true");
                assertThat(request.getParameter("isActive")).isEqualTo("true");
            }
        } finally {
            request.recycle();
        }
    }

    @Test
    void acceptsNoticeReplacingAllFiveAttachments() throws Exception {
        List<FormPart> form = new ArrayList<>(List.of(
                field("title", "Synthetic notice"), field("content", "Synthetic content"),
                field("isPinned", "true")));
        for (int index = 1; index <= ATTACHMENT_MAX_COUNT; index++) {
            form.add(field("deleteAttachmentIds", Integer.toString(index)));
            form.add(file("newAttachments", "synthetic-" + index + ".pdf"));
        }
        assertParsedForm("PUT", form, 13);
    }

    @Test
    void acceptsCertificationWithAllTenDocuments() throws Exception {
        List<FormPart> form = IntStream.rangeClosed(1, CERT_DOC_MAX_COUNT)
                .mapToObj(index -> file("documents", "synthetic-" + index + ".pdf"))
                .toList();
        assertParsedForm("POST", form, 10);
    }

    @Test
    void acceptsQuestionWithAllFiveAttachments() throws Exception {
        List<FormPart> form = new ArrayList<>(List.of(
                field("title", "Synthetic question"), field("content", "Synthetic content"),
                field("category", "OTHER"), field("isPublic", "false")));
        for (int index = 1; index <= ATTACHMENT_MAX_COUNT; index++) {
            form.add(file("attachments", "synthetic-" + index + ".pdf"));
        }
        assertParsedForm("POST", form, 9);
    }

    @Test
    void acceptsExactly128PartsIncludingRepeatedTagFields() throws Exception {
        assertParsedForm("PUT", trackForm("PUT", MAX_PARTS - TRACK_EDIT_FIXED_PARTS), MAX_PARTS);
    }

    @Test
    void rejects129PartsAtConnectorParsingBeforeApplicationHandling() throws Exception {
        Request request = request(configuredConnector(), "PUT",
                trackForm("PUT", MAX_PARTS - TRACK_EDIT_FIXED_PARTS + 1));

        try {
            assertThatThrownBy(request::getParts)
                    .isInstanceOfSatisfying(InvalidParameterException.class,
                            exception -> assertThat(exception.getErrorCode()).isEqualTo(413))
                    .hasCauseInstanceOf(FileCountLimitExceededException.class);
        } finally {
            request.recycle();
        }
    }

    @Test
    void realSpringResolverAndProductionHandlerReturnExisting413Contract() throws Exception {
        Request request = request(configuredConnector(), "PUT",
                trackForm("PUT", MAX_PARTS - TRACK_EDIT_FIXED_PARTS + 1));

        try {
            MaxUploadSizeExceededException failure = assertThrows(MaxUploadSizeExceededException.class,
                    () -> new StandardServletMultipartResolver().resolveMultipart(request));
            assertThat(failure).hasRootCauseInstanceOf(FileCountLimitExceededException.class);

            ResponseEntity<ExceptionResponseDTO> response = new GlobalExceptionHandler().handleAllExceptions(failure);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE);
            assertThat(response.getBody()).isNotNull().satisfies(body -> {
                assertThat(body.getStatus()).isEqualTo(413);
                assertThat(body.getError()).isEqualTo(HttpStatus.CONTENT_TOO_LARGE.getReasonPhrase());
                assertThat(body.getErrorCode()).isEqualTo("IO_LARGE");
                assertThat(body.getMessage()).isEqualTo(BUSINESS_ERROR.IO_LARGE.getClientMessage());
            });
        } finally {
            request.recycle();
        }
    }

    @Test
    void originalUnlimitedSettingAcceptsTheSameOvercountControl() throws Exception {
        Connector connector = configuredConnector();
        connector.setMaxPartCount(-1);
        Request request = request(connector, "PUT",
                trackForm("PUT", MAX_PARTS - TRACK_EDIT_FIXED_PARTS + 1));

        try {
            assertThat(request.getParts()).hasSize(MAX_PARTS + 1);
        } finally {
            request.recycle();
        }
    }

    private void assertParsedForm(String method, List<FormPart> form, int expectedCount) throws Exception {
        Request request = request(configuredConnector(), method, form);
        try {
            assertThat(request.getParts()).hasSize(expectedCount);
            assertThat(request.getParts()).extracting(Part::getName)
                    .containsExactlyElementsOf(form.stream().map(FormPart::name).toList());
        } finally {
            request.recycle();
        }
    }

    private Connector configuredConnector() {
        ConnectorOnlyFactory factory = new ConnectorOnlyFactory();
        new AppConfig().tomcatMaxPartCountCustomizer().customize(factory);
        return factory.customizedConnector();
    }

    private Request request(Connector connector, String method, List<FormPart> form) throws Exception {
        byte[] body = encode(form);
        org.apache.coyote.Request coyoteRequest = new org.apache.coyote.Request();
        coyoteRequest.setResponse(new org.apache.coyote.Response());
        coyoteRequest.method().setString(method);
        coyoteRequest.getMimeHeaders().setValue("content-type")
                .setString("multipart/form-data; boundary=" + BOUNDARY);
        coyoteRequest.setContentLength(body.length);
        ByteBuffer input = ByteBuffer.wrap(body);
        coyoteRequest.setInputBuffer(new InputBuffer() {
            @Override
            public int doRead(ApplicationBufferHandler handler) {
                if (!input.hasRemaining()) {
                    return -1;
                }
                handler.setByteBuffer(input);
                return input.remaining();
            }

            @Override
            public int available() {
                return input.remaining();
            }
        });

        // Only container metadata is mocked; Request.getParts uses Tomcat's real parser.
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(mock(Log.class));
        Wrapper wrapper = mock(Wrapper.class);
        when(wrapper.getMultipartConfigElement()).thenReturn(new MultipartConfigElement(
                uploadDirectory.toAbsolutePath().toString(), 30L * 1024 * 1024, 60L * 1024 * 1024, 0));

        Request request = new Request(connector, coyoteRequest);
        request.getMappingData().context = context;
        request.getMappingData().wrapper = wrapper;
        request.setCharacterEncoding(StandardCharsets.UTF_8);
        return request;
    }

    private static List<FormPart> trackForm(String method, int tagCount) {
        // Mirrors TrackUploadPage / TrackEditPage FormData, including repeated tagIds.
        List<FormPart> form = new ArrayList<>(List.of(
                field("title", "Synthetic track"), field("bpm", "120"),
                field("tonality", "C"), field("description", "Synthetic description")));
        if ("PUT".equals(method)) {
            form.add(field("isActive", "true"));
            form.add(field("replaceTags", "true"));
        }
        form.add(file("audioFile", "synthetic.wav"));
        form.add(file("thumbnail", "synthetic.png"));
        for (int tagID = 1; tagID <= tagCount; tagID++) {
            form.add(field("tagIds", Integer.toString(tagID)));
        }
        return form;
    }

    private static byte[] encode(List<FormPart> form) {
        StringBuilder body = new StringBuilder();
        for (FormPart part : form) {
            body.append("--").append(BOUNDARY).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"").append(part.name()).append('"');
            if (part.filename() != null) {
                body.append("; filename=\"").append(part.filename()).append('"')
                        .append("\r\nContent-Type: application/octet-stream");
            }
            body.append("\r\n\r\n").append(part.value()).append("\r\n");
        }
        return body.append("--").append(BOUNDARY).append("--\r\n")
                .toString().getBytes(StandardCharsets.UTF_8);
    }

    private static FormPart field(String name, String value) {
        return new FormPart(name, null, value);
    }

    private static FormPart file(String name, String filename) {
        return new FormPart(name, filename, "synthetic-file-content");
    }

    private record FormPart(String name, String filename, String value) {}

    private static class ConnectorOnlyFactory extends TomcatServletWebServerFactory {
        Connector customizedConnector() {
            Connector connector = new Connector();
            customizeConnector(connector);
            return connector;
        }
    }
}
