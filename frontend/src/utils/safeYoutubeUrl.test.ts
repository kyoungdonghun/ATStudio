import { describe, expect, it } from 'vitest';
import { getSafeYoutubeUrl } from './safeYoutubeUrl';

describe('getSafeYoutubeUrl', () => {
  it.each([
    'https://youtube.com/@atm',
    'https://www.youtube.com/channel/UC123',
    'HTTPS://WWW.YOUTUBE.COM/@atm',
    'https://youtube.com:443/@atm',
  ])('accepts safe YouTube HTTPS URLs: %s', (value) => {
    expect(getSafeYoutubeUrl(value)).not.toBeNull();
  });

  it.each([
    'javascript://youtube.com/%0Aalert(1)',
    'data://youtube.com/text/html,test',
    'file://youtube.com/test',
    'ftp://youtube.com/test',
    'https://user:password@youtube.com/@atm',
    'https://notyoutube.com/@atm',
    'https://youtube.com.evil.test/@atm',
    'https://youtube.com:8443/@atm',
    'not a url',
  ])('rejects unsafe or lookalike URLs: %s', (value) => {
    expect(getSafeYoutubeUrl(value)).toBeNull();
  });
});
