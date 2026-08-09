package com.atstudio.atstudio.service.image;

import com.atstudio.atstudio.common.exception.BUSINESS_ERROR;
import com.atstudio.atstudio.common.exception.BusinessException;
import com.atstudio.atstudio.common.validation.ValidationConstants;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

@Service
public class CanonicalImageService {

    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private static final int MAX_DIMENSION = 4096;
    private static final long MAX_PIXELS = 16_777_216L;
    private static final int MAX_OUTPUT_DIMENSION = 2048;
    private static final float JPEG_QUALITY = 0.90f;

    public MultipartFile canonicalizeThumbnail(MultipartFile file) {
        return canonicalize(file, ThumbnailPolicy.ANY_ASPECT_RATIO);
    }

    public MultipartFile canonicalizeSquareTrackThumbnail(MultipartFile file) {
        return canonicalize(file, ThumbnailPolicy.SQUARE_TRACK);
    }

    private MultipartFile canonicalize(MultipartFile file, ThumbnailPolicy policy) {
        if (file == null || file.isEmpty()) {
            throw invalidImage();
        }
        if (file.getSize() > ValidationConstants.IMAGE_MAX_SIZE_BYTES) {
            throw new BusinessException(BUSINESS_ERROR.IO_LARGE);
        }

        byte[] input = readBytes(file);
        ImageFormat format = verifySignature(input);
        verifyClientMime(file.getContentType(), format);
        if (format == ImageFormat.PNG) {
            rejectApng(input);
        }

        byte[] canonicalBytes = encodeCanonicalJpeg(input, format, policy);
        return new CanonicalMultipartFile(file.getName(), canonicalBytes);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw invalidImage();
        }
    }

    private ImageFormat verifySignature(byte[] input) {
        if (startsWith(input, JPEG_SIGNATURE)) {
            return ImageFormat.JPEG;
        }
        if (startsWith(input, PNG_SIGNATURE)) {
            return ImageFormat.PNG;
        }
        throw invalidImage();
    }

    private boolean startsWith(byte[] input, byte[] signature) {
        return input.length >= signature.length
                && Arrays.equals(Arrays.copyOf(input, signature.length), signature);
    }

    private void verifyClientMime(String clientMime, ImageFormat format) {
        if (clientMime == null || clientMime.isBlank()) {
            return;
        }
        String normalized = clientMime.toLowerCase(Locale.ROOT);
        if (normalized.equals("application/octet-stream") || normalized.equals(format.mimeType)) {
            return;
        }
        throw invalidImage();
    }

    private void rejectApng(byte[] input) {
        int offset = PNG_SIGNATURE.length;
        while (offset + 12 <= input.length) {
            long chunkLength = readUnsignedInt(input, offset);
            if (chunkLength > Integer.MAX_VALUE || offset + 12L + chunkLength > input.length) {
                throw invalidImage();
            }

            String chunkType = new String(input, offset + 4, 4, java.nio.charset.StandardCharsets.US_ASCII);
            if ("acTL".equals(chunkType)) {
                throw invalidImage();
            }
            offset += 12 + (int) chunkLength;
            if ("IEND".equals(chunkType)) {
                return;
            }
        }
        throw invalidImage();
    }

    private long readUnsignedInt(byte[] input, int offset) {
        return ((long) input[offset] & 0xFF) << 24
                | ((long) input[offset + 1] & 0xFF) << 16
                | ((long) input[offset + 2] & 0xFF) << 8
                | ((long) input[offset + 3] & 0xFF);
    }

    private byte[] encodeCanonicalJpeg(
            byte[] input,
            ImageFormat format,
            ThumbnailPolicy policy) {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(format.readerFormatName);
        if (!readers.hasNext()) {
            throw invalidImage();
        }

        ImageReader reader = readers.next();
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(new ByteArrayInputStream(input))) {
            reader.setInput(imageInput, false, true);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);
            validateBounds(width, height);
            rejectUnsupportedFrameCount(reader);

            BufferedImage decoded = reader.read(0);
            if (decoded == null) {
                throw invalidImage();
            }

            int decodedWidth = decoded.getWidth();
            int decodedHeight = decoded.getHeight();
            validateBounds(decodedWidth, decodedHeight);
            if (policy.requiresSquare && decodedWidth != decodedHeight) {
                throw new BusinessException(BUSINESS_ERROR.TRACK_THUMBNAIL_NOT_SQUARE);
            }

            return writeJpeg(renderRgb(decoded, decodedWidth, decodedHeight));
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            throw invalidImage();
        } finally {
            reader.dispose();
        }
    }

    private void rejectUnsupportedFrameCount(ImageReader reader) throws IOException {
        int imageCount = reader.getNumImages(true);
        if (imageCount != 1) {
            throw invalidImage();
        }
    }

    private void validateBounds(int width, int height) {
        long pixels = (long) width * (long) height;
        if (width < 1
                || height < 1
                || width > MAX_DIMENSION
                || height > MAX_DIMENSION
                || pixels > MAX_PIXELS) {
            throw invalidImage();
        }
    }

    private BufferedImage renderRgb(BufferedImage source, int width, int height) {
        double scale = Math.min(1.0, (double) MAX_OUTPUT_DIMENSION / Math.max(width, height));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = output.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, targetWidth, targetHeight);
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return output;
    }

    private byte[] writeJpeg(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw invalidImage();
        }

        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ImageOutputStream imageOutput = ImageIO.createImageOutputStream(bytes)) {
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(JPEG_QUALITY);
            writer.setOutput(imageOutput);
            writer.write(null, new IIOImage(image, null, null), params);
            imageOutput.flush();
            return bytes.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    private BusinessException invalidImage() {
        return new BusinessException(BUSINESS_ERROR.INVALID_VALID);
    }

    private enum ImageFormat {
        JPEG("jpeg", "image/jpeg"),
        PNG("png", "image/png");

        private final String readerFormatName;
        private final String mimeType;

        ImageFormat(String readerFormatName, String mimeType) {
            this.readerFormatName = readerFormatName;
            this.mimeType = mimeType;
        }
    }

    private enum ThumbnailPolicy {
        ANY_ASPECT_RATIO(false),
        SQUARE_TRACK(true);

        private final boolean requiresSquare;

        ThumbnailPolicy(boolean requiresSquare) {
            this.requiresSquare = requiresSquare;
        }
    }

    private record CanonicalMultipartFile(String name, byte[] bytes) implements MultipartFile {

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return "thumbnail.jpg";
        }

        @Override
        public String getContentType() {
            return "image/jpeg";
        }

        @Override
        public boolean isEmpty() {
            return bytes.length == 0;
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public byte[] getBytes() {
            return bytes.clone();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            java.nio.file.Files.write(dest.toPath(), bytes);
        }
    }
}
