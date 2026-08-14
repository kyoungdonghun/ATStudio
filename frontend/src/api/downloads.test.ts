import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosHeaders } from 'axios';
import {
  createDownloadFallbackFileName,
  normalizeBinaryDownload,
  triggerBlobDownload,
} from '@/api/downloads';

describe('binary download normalization', () => {
  it('prefers a decoded RFC 5987 filename and the valid response content type', () => {
    const blob = new Blob(['audio'], { type: 'audio/mpeg' });

    expect(
      normalizeBinaryDownload(
        {
          data: blob,
          headers: {
            'content-disposition': "attachment; filename*=UTF-8''mix%20track.wav",
            'content-type': 'audio/wav; charset=binary',
          },
        },
        'track-9-mix.mp3',
      ),
    ).toMatchObject({ blob, fileName: 'mix track.wav', contentType: 'audio/wav' });
  });

  it('normalizes RFC 5987 metadata from an actual AxiosHeaders instance', () => {
    const blob = new Blob(['audio'], { type: 'audio/mpeg' });
    const headers = new AxiosHeaders({
      'Content-Disposition': "attachment; filename*=UTF-8''axios%20mix.wav",
      'Content-Type': 'audio/wav; charset=binary',
    });

    expect(normalizeBinaryDownload({ data: blob, headers }, 'track-17-fallback.mp3')).toMatchObject(
      {
        blob,
        fileName: 'axios mix.wav',
        contentType: 'audio/wav',
      },
    );
  });

  it('accepts a quoted basic filename and uses Blob type when the header is invalid', () => {
    const blob = new Blob(['notice'], { type: 'application/pdf' });

    expect(
      normalizeBinaryDownload(
        {
          data: blob,
          headers: {
            'content-disposition': 'attachment; filename="notice.pdf"',
            'content-type': 'application/pdf\r\nX-Unsafe: value',
          },
        },
        'notice-11-fallback.txt',
      ),
    ).toMatchObject({ blob, fileName: 'notice.pdf', contentType: 'application/pdf' });
  });

  it('falls back from malformed or traversal-like filenames and follows a proven media extension', () => {
    const blob = new Blob(['audio']);

    expect(
      normalizeBinaryDownload(
        {
          data: blob,
          headers: {
            'content-disposition': "attachment; filename*=UTF-8''..%2Funsafe%00.mp3",
            'content-type': 'audio/wav',
          },
        },
        createDownloadFallbackFileName('track', 9, 'Mix title', 'mp3'),
      ),
    ).toMatchObject({ fileName: 'track-9-Mix title.wav', contentType: 'audio/wav' });
  });

  it('uses application/octet-stream only when no valid header or Blob type exists', () => {
    expect(
      normalizeBinaryDownload(
        { data: new Blob(['data']), headers: {} },
        createDownloadFallbackFileName('attachment', 11, 'document'),
      ),
    ).toMatchObject({ contentType: 'application/octet-stream' });
  });

  it.each([
    ['Cc', '\u0000'],
    ['Cf', '\u202E'],
    ['Cs', '\uD800'],
    ['Co', '\uE000'],
    ['Cn', '\u0378'],
  ])('rejects Unicode %s characters in fallback filenames', (_category, character) => {
    expect(
      normalizeBinaryDownload(
        {
          data: new Blob(['audio'], { type: 'audio/mpeg' }),
          headers: { 'content-type': 'audio/mpeg' },
        },
        `fallback${character}attacker.exe`,
      ),
    ).toMatchObject({ fileName: 'download.mp3', contentType: 'audio/mpeg' });
  });

  it.each([
    { data: new Blob([]), headers: {} },
    { data: { not: 'a blob' }, headers: {} },
  ])('rejects an unusable response body before browser activation', (response) => {
    expect(() => normalizeBinaryDownload(response, 'track-9.mp3')).toThrow(
      /DOWNLOAD_(EMPTY|INVALID)_BODY/,
    );
  });
});

describe('triggerBlobDownload', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('uses the canonical result and revokes its object URL after activation', () => {
    const click = vi.fn();
    const remove = vi.fn();
    const anchor = { href: '', download: '', click, remove };
    vi.spyOn(document, 'createElement').mockReturnValueOnce(anchor as unknown as HTMLAnchorElement);
    vi.spyOn(document.body, 'appendChild').mockImplementationOnce((node) => node);
    vi.spyOn(URL, 'createObjectURL').mockReturnValueOnce('blob:test');
    vi.spyOn(URL, 'revokeObjectURL').mockImplementationOnce(() => undefined);

    const result = {
      blob: new Blob(['audio'], { type: 'audio/mpeg' }),
      fileName: 'track.mp3',
      contentType: 'audio/mpeg',
    };
    triggerBlobDownload(result);

    expect(anchor).toMatchObject({ href: 'blob:test', download: 'track.mp3' });
    expect(click).toHaveBeenCalledOnce();
    expect(remove).toHaveBeenCalledOnce();
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:test');
  });

  it.each([
    {
      disposition: "attachment; filename*=UTF-8''invoice%E2%80%AEgpj.exe",
      attackerFragment: 'invoice',
    },
    {
      disposition: "attachment; filename*=UTF-8''safe%E2%80%8Bname.exe",
      attackerFragment: 'safe',
    },
  ])(
    'rejects decoded Unicode format controls before browser activation',
    ({ disposition, attackerFragment }) => {
      const anchor = { href: '', download: '', click: vi.fn(), remove: vi.fn() };
      vi.spyOn(document, 'createElement').mockReturnValueOnce(
        anchor as unknown as HTMLAnchorElement,
      );
      vi.spyOn(document.body, 'appendChild').mockImplementationOnce((node) => node);
      vi.spyOn(URL, 'createObjectURL').mockReturnValueOnce('blob:unicode-control');
      vi.spyOn(URL, 'revokeObjectURL').mockImplementationOnce(() => undefined);

      const download = normalizeBinaryDownload(
        {
          data: new Blob(['audio'], { type: 'audio/mpeg' }),
          headers: {
            'content-disposition': disposition,
            'content-type': 'audio/mpeg',
          },
        },
        createDownloadFallbackFileName('track', 9, 'Mix title', 'mp3'),
      );
      triggerBlobDownload(download);

      expect(download.fileName).toBe('track-9-Mix title.mp3');
      expect(anchor.download).toBe('track-9-Mix title.mp3');
      expect(anchor.download).not.toContain(attackerFragment);
      expect(anchor.download).not.toMatch(/\.exe$/i);
    },
  );
});
