package com.atstudio.atstudio.service.audio;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AudioAnalysisService {

    private static final int READ_BUFFER_BYTES = 4096;
    private static final int WAVEFORM_POINTS = 200;
    private static final int INITIAL_FRAMES_PER_BUCKET = 1000;
    private static final int MAX_WAVEFORM_BUCKETS = 4096;

    public AudioAnalysisResult analyze(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.EMPTY_FILE,
                    AudioAnalysisFormat.UNKNOWN);
        }
        return analyze(file.getOriginalFilename(), file::getInputStream);
    }

    public AudioAnalysisResult analyze(String filename, Resource resource) {
        if (resource == null) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.UNREADABLE,
                    AudioAnalysisFormat.fromFilename(filename));
        }
        return analyze(filename, resource::getInputStream);
    }

    private AudioAnalysisResult analyze(String filename, InputStreamSource source) {
        AudioAnalysisFormat format = AudioAnalysisFormat.fromFilename(filename);
        if (format == AudioAnalysisFormat.UNKNOWN) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.UNSUPPORTED_FORMAT,
                    format);
        }

        try (InputStream raw = source.open();
             BufferedInputStream buffered = new BufferedInputStream(raw, 65_536)) {
            validateContainerHeader(buffered, format);
            try (AudioInputStream encoded = AudioSystem.getAudioInputStream(buffered)) {
                validateEncoding(encoded.getFormat(), format);
                return decode(encoded, format);
            }
        } catch (AudioAnalysisException exception) {
            throw exception;
        } catch (UnsupportedAudioFileException | IllegalArgumentException exception) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.INVALID_AUDIO,
                    format,
                    exception);
        } catch (RuntimeException exception) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.INVALID_AUDIO,
                    format,
                    exception);
        } catch (IOException exception) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.UNREADABLE,
                    format,
                    exception);
        }
    }

    private AudioAnalysisResult decode(
            AudioInputStream encoded,
            AudioAnalysisFormat format) throws IOException {
        AudioFormat sourceFormat = encoded.getFormat();
        float sampleRate = sourceFormat.getSampleRate();
        if (!Float.isFinite(sampleRate) || sampleRate <= 0) {
            sampleRate = sourceFormat.getFrameRate();
        }
        int channels = sourceFormat.getChannels();
        if (!Float.isFinite(sampleRate) || sampleRate <= 0 || channels <= 0 || channels > 32) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.INVALID_AUDIO,
                    format);
        }

        AudioFormat pcmFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                16,
                channels,
                channels * 2,
                sampleRate,
                false);
        if (!AudioSystem.isConversionSupported(pcmFormat, sourceFormat)) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.INVALID_AUDIO,
                    format);
        }

        try (AudioInputStream pcm = AudioSystem.getAudioInputStream(pcmFormat, encoded)) {
            return analyzeDecodedPcm(pcm, pcmFormat, format);
        }
    }

    AudioAnalysisResult analyzeDecodedPcm(
            InputStream pcm,
            AudioFormat pcmFormat,
            AudioAnalysisFormat format) throws IOException {
        int frameSize = pcmFormat.getFrameSize();
        int channels = pcmFormat.getChannels();
        float sampleRate = pcmFormat.getSampleRate();
        if (frameSize != channels * 2
                || channels < 1
                || !Float.isFinite(sampleRate)
                || sampleRate <= 0
                || pcmFormat.isBigEndian()
                || !AudioFormat.Encoding.PCM_SIGNED.equals(pcmFormat.getEncoding())) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.INVALID_AUDIO,
                    format);
        }

        WaveformAccumulator waveform = new WaveformAccumulator();
        byte[] buffer = new byte[READ_BUFFER_BYTES + frameSize];
        int remainder = 0;
        long decodedFrames = 0;

        while (true) {
            int read = pcm.read(buffer, remainder, READ_BUFFER_BYTES);
            if (read < 0) {
                break;
            }
            if (read == 0) {
                continue;
            }

            int available = remainder + read;
            int completeBytes = available - (available % frameSize);
            for (int offset = 0; offset < completeBytes; offset += frameSize) {
                double framePeak = 0.0;
                for (int channel = 0; channel < channels; channel++) {
                    int sampleOffset = offset + channel * 2;
                    short sample = (short) ((buffer[sampleOffset] & 0xFF)
                            | (buffer[sampleOffset + 1] << 8));
                    framePeak = Math.max(framePeak, Math.abs((int) sample) / 32768.0);
                }
                waveform.accept(framePeak);
                decodedFrames++;
            }

            remainder = available - completeBytes;
            if (remainder > 0) {
                System.arraycopy(buffer, completeBytes, buffer, 0, remainder);
            }
        }

        if (remainder != 0) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.INVALID_AUDIO,
                    format);
        }
        if (decodedFrames == 0) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.NO_DECODABLE_FRAMES,
                    format);
        }

        double exactDurationSeconds = decodedFrames / (double) sampleRate;
        long roundedDuration = Math.max(1L, Math.round(exactDurationSeconds));
        if (roundedDuration > Integer.MAX_VALUE) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.INVALID_AUDIO,
                    format);
        }

        return new AudioAnalysisResult(
                (int) roundedDuration,
                waveform.toJson(),
                format,
                decodedFrames,
                Math.round(sampleRate),
                channels);
    }

    private void validateContainerHeader(
            BufferedInputStream input,
            AudioAnalysisFormat format) throws IOException {
        input.mark(16);
        byte[] header = input.readNBytes(12);
        input.reset();

        boolean valid = switch (format) {
            case WAV -> header.length >= 12
                    && matches(header, 0, "RIFF")
                    && matches(header, 8, "WAVE");
            case MP3 -> header.length >= 3
                    && (matches(header, 0, "ID3")
                    || ((header[0] & 0xFF) == 0xFF && (header[1] & 0xE0) == 0xE0));
            case UNKNOWN -> false;
        };
        if (!valid) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.INVALID_AUDIO,
                    format);
        }
    }

    private void validateEncoding(AudioFormat sourceFormat, AudioAnalysisFormat format) {
        String encoding = sourceFormat.getEncoding().toString().toUpperCase(Locale.ROOT);
        boolean mpeg = encoding.contains("MPEG") || encoding.contains("MP3");
        if ((format == AudioAnalysisFormat.MP3 && !mpeg)
                || (format == AudioAnalysisFormat.WAV && mpeg)) {
            throw new AudioAnalysisException(
                    AudioAnalysisException.Reason.INVALID_AUDIO,
                    format);
        }
    }

    private boolean matches(byte[] bytes, int offset, String expected) {
        if (bytes.length < offset + expected.length()) {
            return false;
        }
        for (int index = 0; index < expected.length(); index++) {
            if (bytes[offset + index] != (byte) expected.charAt(index)) {
                return false;
            }
        }
        return true;
    }

    @FunctionalInterface
    private interface InputStreamSource {
        InputStream open() throws IOException;
    }

    private static final class WaveformAccumulator {

        private final List<Double> buckets = new ArrayList<>(MAX_WAVEFORM_BUCKETS);
        private long framesPerBucket = INITIAL_FRAMES_PER_BUCKET;
        private long framesInBucket;
        private double bucketPeak;

        void accept(double peak) {
            bucketPeak = Math.max(bucketPeak, peak);
            framesInBucket++;
            if (framesInBucket >= framesPerBucket) {
                addBucket(bucketPeak);
                framesInBucket = 0;
                bucketPeak = 0.0;
            }
        }

        String toJson() {
            if (framesInBucket > 0) {
                addBucket(bucketPeak);
                framesInBucket = 0;
                bucketPeak = 0.0;
            }

            int total = buckets.size();
            StringBuilder json = new StringBuilder("[");
            for (int point = 0; point < WAVEFORM_POINTS; point++) {
                if (point > 0) {
                    json.append(',');
                }
                int start = (int) ((long) point * total / WAVEFORM_POINTS);
                int end = (int) ((long) (point + 1) * total / WAVEFORM_POINTS);
                if (end <= start) {
                    end = start + 1;
                }
                double peak = 0.0;
                for (int bucket = start; bucket < end && bucket < total; bucket++) {
                    peak = Math.max(peak, buckets.get(bucket));
                }
                json.append(String.format(Locale.ROOT, "%.3f", peak));
            }
            return json.append(']').toString();
        }

        private void addBucket(double peak) {
            buckets.add(peak);
            if (buckets.size() >= MAX_WAVEFORM_BUCKETS) {
                compactBuckets();
            }
        }

        private void compactBuckets() {
            int target = 0;
            for (int source = 0; source < buckets.size(); source += 2) {
                double merged = buckets.get(source);
                if (source + 1 < buckets.size()) {
                    merged = Math.max(merged, buckets.get(source + 1));
                }
                buckets.set(target++, merged);
            }
            buckets.subList(target, buckets.size()).clear();
            if (framesPerBucket <= Long.MAX_VALUE / 2) {
                framesPerBucket *= 2;
            }
        }
    }
}
