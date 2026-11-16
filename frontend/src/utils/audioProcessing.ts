import type { AudioProcessing } from '@/api/tracks';
import { classifyLoadError, getApiErrorCode, isAmbiguousMutationError } from '@/api/loadError';

export function isAudioProcessingPending(value: AudioProcessing): boolean {
  return value.state === 'PENDING' || value.state === 'PROCESSING';
}

export function publicationReadiness(value?: AudioProcessing | null): string | null {
  if (!value) return '변환 상태를 확인하려면 목록을 새로고침해 주세요.';
  if (isAudioProcessingPending(value)) return '변환 완료 후 공개할 수 있습니다.';
  if (value.state !== 'READY') return '음원 변환 상태를 확인해 주세요.';
  if (!value.streamReady) return '재생 파일 준비 후 공개할 수 있습니다.';
  return null;
}

export function audioProcessingFailureMessage(code: AudioProcessing['errorCode']): string {
  switch (code) {
    case 'FFMPEG_NOT_FOUND':
      return '서버의 음원 변환 프로그램을 실행할 수 없습니다. 운영 담당자에게 설치 및 실행 설정 확인을 요청해 주세요.';
    case 'AUDIO_TRANSCODE_TIMEOUT':
      return '음원 변환 제한 시간을 초과했습니다. 서버 부하를 확인한 뒤 다시 시도해 주세요.';
    case 'AUDIO_TRANSCODE_FAILED':
      return '음원 변환에 실패했습니다. 원본 WAV 파일이 정상 재생되는지 확인한 뒤 다시 시도해 주세요.';
    case 'AUDIO_OUTPUT_INVALID':
      return '생성된 재생 파일 검증에 실패했습니다. 다시 시도하고, 반복되면 운영 담당자에게 문의해 주세요.';
    case 'AUDIO_OUTPUT_TOO_LARGE':
      return '예상 재생 파일이 허용 크기를 초과했습니다. 원본 길이와 샘플레이트를 확인해 주세요.';
    case 'AUDIO_INPUT_UNAVAILABLE':
      return '변환할 원본 파일을 읽을 수 없습니다. 운영 담당자에게 원본 파일과 저장소 접근 상태 확인을 요청해 주세요.';
    case 'AUDIO_STORAGE_FAILED':
      return '음원 저장 처리에 실패했습니다. 운영 담당자에게 저장 공간과 접근 권한 확인을 요청해 주세요.';
    case 'AUDIO_PROCESSING_INTERRUPTED':
      return '이전 음원 변환이 중단되었습니다. 서버 상태를 확인한 뒤 다시 시도해 주세요.';
    case 'AUDIO_FORMAT_UNSUPPORTED':
      return '원본 음원 형식을 변환할 수 없습니다. WAV 파일 형식을 확인해 주세요.';
    case 'AUDIO_PROCESS_TERMINATION_FAILED':
      return '음원 변환 프로세스 종료를 확인하지 못했습니다. 운영 담당자에게 프로세스 상태 확인을 요청해 주세요.';
    default:
      return '음원 변환을 완료하지 못했습니다. 원본 파일과 서버 상태를 확인해 주세요.';
  }
}

export function audioProcessingError(error: unknown, fallback: string): string {
  const code = getApiErrorCode(error);
  if (code === 'AUDIO_PROCESSING_CONFLICT') {
    return '음원 처리 상태가 변경되었습니다. 최신 상태를 확인해 주세요.';
  }
  if (code === 'AUDIO_STREAM_NOT_READY') {
    return '재생 파일이 아직 준비되지 않아 활성화할 수 없습니다.';
  }
  if (code === 'AUDIO_ANALYSIS_FAILED') {
    return '음원 파일을 분석하지 못했습니다. MP3 또는 WAV 파일이 정상 재생되는지 확인해 주세요. 정상 파일에서도 반복되면 운영 담당자에게 문의해 주세요.';
  }
  if (code === 'IO_LARGE') {
    return '파일 크기가 허용 한도를 초과했습니다. 음원은 100MB 이하, 썸네일은 10MB 이하로 선택해 주세요.';
  }
  if (code === 'TRACK_THUMBNAIL_NOT_SQUARE') {
    return '트랙 썸네일은 가로와 세로 길이가 같은 1:1 이미지여야 합니다.';
  }
  if (code === 'TAG_NOT_FOUND') {
    return '선택한 태그를 찾을 수 없습니다. 태그 목록을 새로 확인한 뒤 선택해 주세요.';
  }
  return fallback;
}

export function audioMutationError(error: unknown, fallback: string): string {
  const kind = classifyLoadError(error);
  let message = fallback;
  if (kind === 'unauthorized') {
    message =
      '로그인 상태를 확인할 수 없습니다. 다시 로그인한 뒤 음원 관리에서 처리 상태를 확인해 주세요.';
  } else if (kind === 'forbidden') {
    message = '음원을 등록하거나 수정할 권한이 없습니다. 관리자 계정을 확인해 주세요.';
  } else if (isAmbiguousMutationError(error)) {
    message =
      '서버의 처리 결과를 확인하지 못했습니다. 중복 등록을 피하려면 다시 시도하기 전에 음원 관리에서 저장 여부를 확인해 주세요.';
  }
  return audioProcessingError(error, message);
}
