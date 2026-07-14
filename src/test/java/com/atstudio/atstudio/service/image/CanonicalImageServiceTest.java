package com.atstudio.atstudio.service.image;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.common.validation.ValidationConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CanonicalImageService 테스트")
class CanonicalImageServiceTest {

    private final CanonicalImageService service = new CanonicalImageService();

    @Test
    @DisplayName("valid JPEG input becomes canonical JPEG without trailing payload")
    void canonicalizeThumbnail_validJpegWithTrailingPayload_returnsCanonicalJpeg() throws Exception {
        byte[] input = append(jpegBytes(32, 24), "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));
        MultipartFile result = service.canonicalizeThumbnail(file("cover.svg", "image/jpeg", input));

        assertThat(result.getOriginalFilename()).isEqualTo("thumbnail.jpg");
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
        assertThat(result.getBytes()).startsWith((byte) 0xFF, (byte) 0xD8, (byte) 0xFF);
        assertThat(asAscii(result.getBytes())).doesNotContain("<script");
        BufferedImage output = ImageIO.read(new ByteArrayInputStream(result.getBytes()));
        assertThat(output.getWidth()).isEqualTo(32);
        assertThat(output.getHeight()).isEqualTo(24);
        assertThat(output.getColorModel().hasAlpha()).isFalse();
    }

    @Test
    @DisplayName("valid PNG input becomes flattened canonical JPEG without trailing payload")
    void canonicalizeThumbnail_validPngWithTrailingPayload_returnsCanonicalJpeg() throws Exception {
        byte[] input = append(pngBytes(20, 20), "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8));
        MultipartFile result = service.canonicalizeThumbnail(file("cover.png", "image/png", input));

        assertThat(result.getContentType()).isEqualTo("image/jpeg");
        assertThat(result.getBytes()).startsWith((byte) 0xFF, (byte) 0xD8, (byte) 0xFF);
        assertThat(asAscii(result.getBytes())).doesNotContain("<svg");
    }

    @Test
    @DisplayName("valid large image is downscaled without upscaling")
    void canonicalizeThumbnail_largeImage_downscaled() throws Exception {
        MultipartFile result = service.canonicalizeThumbnail(file("large.png", "image/png", pngBytes(3000, 1000)));

        BufferedImage output = ImageIO.read(new ByteArrayInputStream(result.getBytes()));
        assertThat(output.getWidth()).isEqualTo(2048);
        assertThat(output.getHeight()).isEqualTo(683);
    }

    @Test
    @DisplayName("SVG, HTML, GIF, WebP signatures are rejected")
    void canonicalizeThumbnail_activeOrUnsupportedFormats_rejected() {
        byte[][] samples = {
                "<svg onload=alert(1)>".getBytes(StandardCharsets.UTF_8),
                "<html><script>alert(1)</script>".getBytes(StandardCharsets.UTF_8),
                new byte[]{'G', 'I', 'F', '8', '9', 'a', 1, 0, 1, 0},
                new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'}
        };

        for (byte[] sample : samples) {
            assertInvalid(sample);
        }
    }

    @Test
    @DisplayName("client MIME mismatch is rejected")
    void canonicalizeThumbnail_mimeMismatch_rejected() throws Exception {
        assertThatThrownBy(() -> service.canonicalizeThumbnail(file("cover.jpg", "image/png", jpegBytes(10, 10))))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_VALID));
    }

    @Test
    @DisplayName("truncated image is rejected")
    void canonicalizeThumbnail_truncatedSignature_rejected() {
        assertInvalid(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});
    }

    @Test
    @DisplayName("APNG animation marker is rejected")
    void canonicalizeThumbnail_apngRejected() throws Exception {
        byte[] apng = insertAfterPngSignatureAndIhdr(pngBytes(10, 10), pngChunk("acTL", new byte[]{0, 0, 0, 1, 0, 0, 0, 0}));

        assertThatThrownBy(() -> service.canonicalizeThumbnail(file("animated.png", "image/png", apng)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_VALID));
    }

    @Test
    @DisplayName("excessive dimensions are rejected before full decode")
    void canonicalizeThumbnail_excessiveDimensions_rejected() throws Exception {
        byte[] hugeWidthPng = mutatePngWidth(pngBytes(1, 1), 4097);

        assertThatThrownBy(() -> service.canonicalizeThumbnail(file("huge.png", "image/png", hugeWidthPng)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_VALID));
    }

    @Test
    @DisplayName("oversized input is rejected with IO_LARGE")
    void canonicalizeThumbnail_oversizedInput_rejectedWithIoLarge() {
        byte[] oversized = new byte[(int) ValidationConstants.IMAGE_MAX_SIZE_BYTES + 1];

        assertThatThrownBy(() -> service.canonicalizeThumbnail(file("large.png", "image/png", oversized)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.IO_LARGE));
    }

    private void assertInvalid(byte[] bytes) {
        assertThatThrownBy(() -> service.canonicalizeThumbnail(file("payload.bin", "application/octet-stream", bytes)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).getErrorCode())
                        .isEqualTo(BUSINESS_ERROR.INVALID_VALID));
    }

    private MockMultipartFile file(String filename, String contentType, byte[] bytes) {
        return new MockMultipartFile("thumbnail", filename, contentType, bytes);
    }

    private byte[] jpegBytes(int width, int height) throws Exception {
        return imageBytes("jpg", width, height);
    }

    private byte[] pngBytes(int width, int height) throws Exception {
        return imageBytes("png", width, height);
    }

    private byte[] imageBytes(String format, int width, int height) throws Exception {
        int imageType = "jpg".equals(format) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage image = new BufferedImage(width, height, imageType);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, new Color(20, 80, 140, 180).getRGB());
            }
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }

    private byte[] append(byte[] base, byte[] suffix) {
        byte[] result = Arrays.copyOf(base, base.length + suffix.length);
        System.arraycopy(suffix, 0, result, base.length, suffix.length);
        return result;
    }

    private String asAscii(byte[] bytes) {
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    private byte[] insertAfterPngSignatureAndIhdr(byte[] png, byte[] chunk) {
        int insertOffset = 8 + 25;
        byte[] result = new byte[png.length + chunk.length];
        System.arraycopy(png, 0, result, 0, insertOffset);
        System.arraycopy(chunk, 0, result, insertOffset, chunk.length);
        System.arraycopy(png, insertOffset, result, insertOffset + chunk.length, png.length - insertOffset);
        return result;
    }

    private byte[] pngChunk(String type, byte[] data) {
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        CRC32 crc32 = new CRC32();
        crc32.update(typeBytes);
        crc32.update(data);

        ByteBuffer buffer = ByteBuffer.allocate(12 + data.length);
        buffer.putInt(data.length);
        buffer.put(typeBytes);
        buffer.put(data);
        buffer.putInt((int) crc32.getValue());
        return buffer.array();
    }

    private byte[] mutatePngWidth(byte[] png, int width) {
        byte[] result = png.clone();
        ByteBuffer.wrap(result, 16, 4).putInt(width);
        updateIhdrCrc(result);
        return result;
    }

    private void updateIhdrCrc(byte[] png) {
        CRC32 crc32 = new CRC32();
        crc32.update(png, 12, 17);
        ByteBuffer.wrap(png, 29, 4).putInt((int) crc32.getValue());
    }
}
