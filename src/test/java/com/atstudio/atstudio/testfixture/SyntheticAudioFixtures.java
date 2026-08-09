package com.atstudio.atstudio.testfixture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class SyntheticAudioFixtures {

    public static final int MP3_SAMPLE_RATE = 44_100;
    public static final int MP3_SAMPLES_PER_FRAME = 1_152;

    private SyntheticAudioFixtures() {
    }

    public static byte[] wav16(int sampleRate, int channels, int frames) {
        int frameSize = channels * 2;
        int dataSize = frames * frameSize;
        ByteArrayOutputStream out = new ByteArrayOutputStream(44 + dataSize);
        try {
            out.write("RIFF".getBytes(StandardCharsets.US_ASCII));
            writeLittleEndian(out, 36 + dataSize, 4);
            out.write("WAVE".getBytes(StandardCharsets.US_ASCII));
            out.write("fmt ".getBytes(StandardCharsets.US_ASCII));
            writeLittleEndian(out, 16, 4);
            writeLittleEndian(out, 1, 2);
            writeLittleEndian(out, channels, 2);
            writeLittleEndian(out, sampleRate, 4);
            writeLittleEndian(out, sampleRate * frameSize, 4);
            writeLittleEndian(out, frameSize, 2);
            writeLittleEndian(out, 16, 2);
            out.write("data".getBytes(StandardCharsets.US_ASCII));
            writeLittleEndian(out, dataSize, 4);
            for (int frame = 0; frame < frames; frame++) {
                for (int channel = 0; channel < channels; channel++) {
                    double level = channel == 0 ? 0.5 : -0.75;
                    writeLittleEndian(out, (short) Math.round(32_767 * level), 2);
                }
            }
        } catch (IOException impossible) {
            throw new IllegalStateException(impossible);
        }
        return out.toByteArray();
    }

    public static byte[] mp3Cbr(int bitrateKbps, int frameCount) {
        int[] bitrates = new int[frameCount];
        Arrays.fill(bitrates, bitrateKbps);
        return mp3Frames(bitrates, false, false);
    }

    public static byte[] mp3VbrWithId3(int frameCount) {
        int[] pattern = {64, 96, 128, 192, 320};
        int[] bitrates = new int[frameCount];
        for (int index = 0; index < frameCount; index++) {
            bitrates[index] = pattern[index % pattern.length];
        }
        return mp3Frames(bitrates, true, true);
    }

    private static byte[] mp3Frames(int[] bitrates, boolean includeId3, boolean includeXing) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if (includeId3) {
                out.write(id3v23Title("Synthetic VBR fixture"));
            }
            for (int frameIndex = 0; frameIndex < bitrates.length; frameIndex++) {
                int bitrateIndex = bitrateIndex(bitrates[frameIndex]);
                int frameLength = (144_000 * bitrates[frameIndex]) / MP3_SAMPLE_RATE;
                byte[] frame = new byte[frameLength];
                frame[0] = (byte) 0xFF;
                frame[1] = (byte) 0xFB;
                frame[2] = (byte) (bitrateIndex << 4);
                frame[3] = (byte) 0xC0;

                if (frameIndex == 0 && includeXing) {
                    int xingOffset = 4 + 17;
                    writeAscii(frame, xingOffset, "Xing");
                    writeBigEndian(frame, xingOffset + 4, 1);
                    writeBigEndian(frame, xingOffset + 8, bitrates.length);
                }
                out.write(frame);
            }
        } catch (IOException impossible) {
            throw new IllegalStateException(impossible);
        }
        return out.toByteArray();
    }

    private static byte[] id3v23Title(String title) throws IOException {
        byte[] text = title.getBytes(StandardCharsets.ISO_8859_1);
        int framePayloadSize = 1 + text.length;
        int bodySize = 10 + framePayloadSize;
        ByteArrayOutputStream out = new ByteArrayOutputStream(10 + bodySize);
        out.write("ID3".getBytes(StandardCharsets.US_ASCII));
        out.write(3);
        out.write(0);
        out.write(0);
        writeSyncSafe(out, bodySize);
        out.write("TIT2".getBytes(StandardCharsets.US_ASCII));
        writeBigEndian(out, framePayloadSize);
        out.write(0);
        out.write(0);
        out.write(0);
        out.write(text);
        return out.toByteArray();
    }

    private static int bitrateIndex(int bitrateKbps) {
        return switch (bitrateKbps) {
            case 32 -> 1;
            case 40 -> 2;
            case 48 -> 3;
            case 56 -> 4;
            case 64 -> 5;
            case 80 -> 6;
            case 96 -> 7;
            case 112 -> 8;
            case 128 -> 9;
            case 160 -> 10;
            case 192 -> 11;
            case 224 -> 12;
            case 256 -> 13;
            case 320 -> 14;
            default -> throw new IllegalArgumentException("Unsupported synthetic MP3 bitrate");
        };
    }

    private static void writeAscii(byte[] target, int offset, String value) {
        byte[] encoded = value.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(encoded, 0, target, offset, encoded.length);
    }

    private static void writeBigEndian(byte[] target, int offset, int value) {
        target[offset] = (byte) (value >>> 24);
        target[offset + 1] = (byte) (value >>> 16);
        target[offset + 2] = (byte) (value >>> 8);
        target[offset + 3] = (byte) value;
    }

    private static void writeBigEndian(ByteArrayOutputStream out, int value) {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private static void writeSyncSafe(ByteArrayOutputStream out, int value) {
        out.write((value >>> 21) & 0x7F);
        out.write((value >>> 14) & 0x7F);
        out.write((value >>> 7) & 0x7F);
        out.write(value & 0x7F);
    }

    private static void writeLittleEndian(ByteArrayOutputStream out, int value, int bytes) {
        for (int index = 0; index < bytes; index++) {
            out.write((value >>> (index * 8)) & 0xFF);
        }
    }
}
