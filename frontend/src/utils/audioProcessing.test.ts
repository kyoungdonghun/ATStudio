import { describe, expect, it } from 'vitest';
import { audioMutationError } from './audioProcessing';

describe('audioMutationError safe mutation guidance', () => {
  it.each([
    ['AUDIO_ANALYSIS_FAILED', '음원 파일을 분석하지 못했습니다.'],
    ['IO_LARGE', '음원은 100MB 이하'],
    ['TRACK_THUMBNAIL_NOT_SQUARE', '1:1 이미지'],
    ['TAG_NOT_FOUND', '선택한 태그를 찾을 수 없습니다.'],
    ['AUDIO_PROCESSING_CONFLICT', '음원 처리 상태가 변경되었습니다.'],
    ['AUDIO_STREAM_NOT_READY', '재생 파일이 아직 준비되지 않아'],
  ])('maps %s without exposing diagnostic fields', (errorCode, expected) => {
    const result = audioMutationError(
      {
        message: 'unsafe client exception',
        response: {
          status: 400,
          data: { errorCode, message: 'unsafe server detail', path: '/private/audio.wav' },
        },
      },
      'safe fallback',
    );
    expect(result).toContain(expected);
    expect(result).not.toMatch(/unsafe|private|audio\.wav/);
  });

  it.each(['UNKNOWN_ERROR', '<script>private</script>', 123, null, ['AUDIO_ANALYSIS_FAILED']])(
    'keeps unrecognized or malformed code %j behind the safe fallback',
    (errorCode) => {
      expect(
        audioMutationError(
          { response: { status: 400, data: { errorCode, message: 'private diagnostic' } } },
          'safe fallback',
        ),
      ).toBe('safe fallback');
    },
  );

  it.each([
    new Error('private network detail'),
    { code: 'ECONNABORTED', message: 'private timeout detail' },
    { response: { status: 503, data: { message: 'private server detail' } } },
  ])('requires saved-state reconciliation for uncertain outcomes', (error) => {
    const result = audioMutationError(error, 'safe fallback');
    expect(result).toContain('다시 시도하기 전에 음원 관리에서 저장 여부를 확인');
    expect(result).not.toContain('private');
  });

  it.each([
    [401, '다시 로그인한 뒤 음원 관리에서 처리 상태'],
    [403, '관리자 계정을 확인'],
  ])('gives safe access guidance for status %s', (status, expected) => {
    expect(
      audioMutationError({ response: { status, data: { message: 'secret' } } }, 'fallback'),
    ).toContain(expected);
  });
});
