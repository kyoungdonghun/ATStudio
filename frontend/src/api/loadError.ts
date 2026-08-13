import axios from 'axios';

export type LoadErrorKind =
  | 'cancelled'
  | 'unauthorized'
  | 'forbidden'
  | 'not-found'
  | 'validation'
  | 'server'
  | 'network'
  | 'unknown';

interface ErrorShape {
  code?: string;
  name?: string;
  response?: {
    status?: number;
    data?: {
      errorCode?: string;
    };
  };
}

export function getApiErrorCode(error: unknown): string | null {
  const data = (error as ErrorShape)?.response?.data;
  return typeof data?.errorCode === 'string' ? data.errorCode : null;
}

export function classifyLoadError(error: unknown): LoadErrorKind {
  const shaped = error as ErrorShape;

  if (
    axios.isCancel(error) ||
    shaped?.code === 'ERR_CANCELED' ||
    shaped?.name === 'CanceledError' ||
    shaped?.name === 'AbortError'
  ) {
    return 'cancelled';
  }

  const status = shaped?.response?.status;
  if (status === 401) return 'unauthorized';
  if (status === 403) return 'forbidden';
  if (status === 404) return 'not-found';
  if (status === 400 || status === 409 || status === 422) return 'validation';
  if (typeof status === 'number' && status >= 500) return 'server';

  if (shaped?.code === 'ECONNABORTED' || shaped?.code === 'ETIMEDOUT' || !shaped?.response) {
    return 'network';
  }

  return 'unknown';
}

export function isAmbiguousMutationError(error: unknown): boolean {
  const kind = classifyLoadError(error);
  return kind === 'network' || kind === 'server' || kind === 'unknown';
}

export function getLoadErrorMessageForKind(kind: LoadErrorKind, subject: string): string {
  switch (kind) {
    case 'unauthorized':
      return `${subject} 정보를 불러오는 데 필요한 로그인 상태를 확인하지 못했습니다. 다시 시도해주세요.`;
    case 'forbidden':
      return `${subject} 정보에 접근할 권한이 없습니다.`;
    case 'not-found':
      return `${subject} 정보를 찾을 수 없습니다.`;
    case 'validation':
      return `${subject} 요청 정보를 확인할 수 없습니다.`;
    case 'server':
      return `${subject} 정보를 불러오는 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.`;
    case 'network':
      return `${subject} 정보를 불러오지 못했습니다. 네트워크 연결을 확인하고 다시 시도해주세요.`;
    default:
      return `${subject} 정보를 불러오지 못했습니다. 다시 시도해주세요.`;
  }
}

export function getLoadErrorMessage(error: unknown, subject: string): string {
  return getLoadErrorMessageForKind(classifyLoadError(error), subject);
}
