import { afterEach, describe, expect, it, vi } from 'vitest';

import {
  AUDIO_ACCEPT,
  AUDIO_FORMAT_LABEL,
  formatPhone,
  getAudioAccept,
  hasValidAudioExtension,
  isFileSizeOk,
  isIOS,
  isValidEmail,
  isValidNickname,
  isValidPassword,
  isValidPhone,
  validateCompanyCertFileSelection,
  validateImageDimensions,
} from '@/utils/validation';

function sizedFile(name: string, size: number): File {
  const file = new File(['x'], name);
  Object.defineProperty(file, 'size', { value: size });
  return file;
}

describe('shared validation helpers', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it.each([
    ['02', '02'],
    ['02123', '02-123'],
    ['021234567', '02-123-4567'],
    ['0212345678', '02-1234-5678'],
    ['010', '010'],
    ['0101234', '010-1234'],
    ['01012345678', '010-1234-5678'],
    ['010-1234-abcd-5678', '010-1234-5678'],
  ])('formats %s as %s', (input, expected) => {
    expect(formatPhone(input)).toBe(expected);
  });

  it('validates account fields at both accepted and rejected boundaries', () => {
    expect(isValidEmail('user@example.com')).toBe(true);
    expect(isValidEmail('user@invalid')).toBe(false);
    expect(isValidPhone('010-1234-5678')).toBe(true);
    expect(isValidPhone('01012345678')).toBe(false);
    expect(isValidNickname('AT_M')).toBe(true);
    expect(isValidNickname('x')).toBe(false);
    expect(isValidNickname('x'.repeat(21))).toBe(false);
    expect(isValidNickname('bad nickname')).toBe(false);
    expect(isValidPassword('12345678')).toBe(true);
    expect(isValidPassword('short')).toBe(false);
    expect(isValidPassword('x'.repeat(101))).toBe(false);
  });

  it('validates file size and audio extensions case-insensitively', () => {
    expect(isFileSizeOk(sizedFile('small.pdf', 1024), 1)).toBe(true);
    expect(isFileSizeOk(sizedFile('large.pdf', 2 * 1024 * 1024), 1)).toBe(false);
    expect(hasValidAudioExtension('TRACK.MP3')).toBe(true);
    expect(hasValidAudioExtension('mix.WAV')).toBe(true);
    expect(hasValidAudioExtension('track.m4a')).toBe(false);
    expect(hasValidAudioExtension('track.aac')).toBe(false);
    expect(hasValidAudioExtension('track.flac')).toBe(false);
    expect(hasValidAudioExtension('track.ogg')).toBe(false);
    expect(hasValidAudioExtension('track')).toBe(false);
    expect(hasValidAudioExtension('track.exe')).toBe(false);
    expect(AUDIO_ACCEPT).toBe('.mp3,.wav,audio/mpeg,audio/wav,audio/x-wav');
    expect(AUDIO_FORMAT_LABEL).toBe('MP3, WAV');
  });

  it('accepts a valid company certification file selection', () => {
    expect(
      validateCompanyCertFileSelection(
        [sizedFile('existing.pdf', 1024)],
        [sizedFile('new.JPEG', 2048)],
      ),
    ).toBeNull();
  });

  it('detects iOS user agents and touch-enabled iPad desktop mode', () => {
    Object.defineProperty(navigator, 'userAgent', { configurable: true, value: 'iPhone' });
    expect(isIOS()).toBe(true);
    expect(getAudioAccept()).toBeUndefined();
    Object.defineProperty(navigator, 'userAgent', { configurable: true, value: 'Desktop' });
    Object.defineProperty(navigator, 'platform', { configurable: true, value: 'MacIntel' });
    Object.defineProperty(navigator, 'maxTouchPoints', { configurable: true, value: 5 });
    expect(isIOS()).toBe(true);
    expect(getAudioAccept()).toBeUndefined();
    Object.defineProperty(navigator, 'maxTouchPoints', { configurable: true, value: 0 });
    expect(isIOS()).toBe(false);
    expect(getAudioAccept()).toBe(AUDIO_ACCEPT);
  });

  it.each([
    [100, 300, 'size'],
    [800, 200, 'ratio'],
    [400, 400, null],
  ])('validates loaded image dimensions %sx%s', async (width, height, expectedKind) => {
    class TestImage {
      width = width;
      height = height;
      src = '';
      onload: (() => void) | null = null;
      onerror: (() => void) | null = null;

      constructor() {
        queueMicrotask(() => this.onload?.());
      }
    }
    vi.stubGlobal('Image', TestImage);
    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:image');
    vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
    const result = await validateImageDimensions(new File(['image'], 'cover.png'));
    if (expectedKind === null) expect(result).toBeNull();
    else expect(result).toEqual(expect.any(String));
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:image');
  });

  it('returns an error when the browser cannot decode an image', async () => {
    class BrokenImage {
      src = '';
      onload: (() => void) | null = null;
      onerror: (() => void) | null = null;

      constructor() {
        queueMicrotask(() => this.onerror?.());
      }
    }
    vi.stubGlobal('Image', BrokenImage);
    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:broken');
    vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
    await expect(validateImageDimensions(new File(['bad'], 'bad.png'))).resolves.toEqual(
      expect.any(String),
    );
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:broken');
  });
});
