import { classifyLoadError, getApiErrorCode } from '@/api/loadError';

type ErrorMessages = Readonly<Record<string, string>>;

interface SafeErrorOptions {
  codeMessages: ErrorMessages;
  fallback: string;
  network: string;
  server: string;
}

function getSafeAuthErrorMessage(error: unknown, options: SafeErrorOptions): string {
  const errorCode = getApiErrorCode(error);
  if (errorCode && Object.prototype.hasOwnProperty.call(options.codeMessages, errorCode)) {
    const mappedMessage = options.codeMessages[errorCode];
    if (typeof mappedMessage === 'string') return mappedMessage;
  }

  switch (classifyLoadError(error)) {
    case 'network':
      return options.network;
    case 'server':
      return options.server;
    default:
      return options.fallback;
  }
}

const RATE_LIMIT_MESSAGE = '짧은 시간에 요청이 너무 많았습니다. 잠시 후 다시 시도해주세요.';
const PASSWORD_LOGIN_DISABLED_MESSAGE =
  '현재 이 환경에서는 이메일 로그인과 비밀번호 기능이 비활성화되어 있습니다.';
const INPUT_MESSAGE = '입력값을 확인하고 다시 시도해주세요.';

export function getForgotPasswordErrorMessage(error: unknown): string {
  return getSafeAuthErrorMessage(error, {
    codeMessages: {
      RATE_LIMIT_EXCEEDED: RATE_LIMIT_MESSAGE,
      PASSWORD_LOGIN_DISABLED: PASSWORD_LOGIN_DISABLED_MESSAGE,
      INVALID_ARGUMENT: INPUT_MESSAGE,
      INVALID_VALID: INPUT_MESSAGE,
      INVALID_VALIDATED: INPUT_MESSAGE,
    },
    fallback: '비밀번호 재설정 요청에 실패했습니다. 잠시 후 다시 시도해주세요.',
    network: '네트워크 연결을 확인하고 비밀번호 재설정을 다시 요청해주세요.',
    server: '서버 오류로 요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.',
  });
}

export function getPasswordUpdateErrorMessage(error: unknown): string {
  return getSafeAuthErrorMessage(error, {
    codeMessages: {
      INVALID_CREDENTIALS: '현재 비밀번호가 올바르지 않습니다.',
      RATE_LIMIT_EXCEEDED: RATE_LIMIT_MESSAGE,
      INVALID_ARGUMENT: '비밀번호 변경에 실패했습니다. 입력값을 확인하고 다시 시도해주세요.',
      INVALID_VALID: '비밀번호 변경에 실패했습니다. 입력값을 확인하고 다시 시도해주세요.',
      INVALID_VALIDATED: '비밀번호 변경에 실패했습니다. 입력값을 확인하고 다시 시도해주세요.',
    },
    fallback: '비밀번호 변경에 실패했습니다. 입력값을 확인하고 다시 시도해주세요.',
    network: '네트워크 연결을 확인하고 비밀번호 변경을 다시 시도해주세요.',
    server: '비밀번호 변경 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
  });
}

const PROFILE_FIELD_MESSAGES: ErrorMessages = {
  NICKNAME_DUPLICATED: '이미 사용 중인 닉네임입니다.',
  PHONE_ALREADY_REGISTERED: '이미 등록된 전화번호입니다.',
  INVALID_ARGUMENT: INPUT_MESSAGE,
  INVALID_VALID: INPUT_MESSAGE,
  INVALID_VALIDATED: INPUT_MESSAGE,
};

export function getCompleteProfileErrorMessage(error: unknown): string {
  return getSafeAuthErrorMessage(error, {
    codeMessages: {
      ...PROFILE_FIELD_MESSAGES,
      PROFILE_ALREADY_COMPLETE: '이미 완성된 프로필입니다. 내 계정으로 이동합니다.',
    },
    fallback: '프로필 완성에 실패했습니다. 입력값을 확인하고 다시 시도해주세요.',
    network: '네트워크 연결을 확인하고 프로필 완성을 다시 시도해주세요.',
    server: '프로필 완성 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
  });
}

export function getProfileUpdateErrorMessage(error: unknown): string {
  return getSafeAuthErrorMessage(error, {
    codeMessages: PROFILE_FIELD_MESSAGES,
    fallback: '프로필 저장에 실패했습니다. 입력값을 확인하고 다시 시도해주세요.',
    network: '네트워크 연결을 확인하고 프로필 저장을 다시 시도해주세요.',
    server: '프로필 저장 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
  });
}

export function getSignupErrorMessage(error: unknown): string {
  return getSafeAuthErrorMessage(error, {
    codeMessages: {
      EMAIL_ALREADY_REGISTERED: '이미 가입된 이메일입니다.',
      ...PROFILE_FIELD_MESSAGES,
      PASSWORD_LOGIN_DISABLED: PASSWORD_LOGIN_DISABLED_MESSAGE,
      RATE_LIMIT_EXCEEDED: RATE_LIMIT_MESSAGE,
    },
    fallback: '회원가입에 실패했습니다. 입력값을 확인하고 다시 시도해주세요.',
    network: '네트워크 연결을 확인하고 회원가입을 다시 시도해주세요.',
    server: '회원가입 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
  });
}

export function getPasswordResetErrorMessage(error: unknown): string {
  return getSafeAuthErrorMessage(error, {
    codeMessages: {
      INVALID_TOKEN: '유효하지 않거나 만료된 재설정 링크입니다.',
      PASSWORD_LOGIN_DISABLED: PASSWORD_LOGIN_DISABLED_MESSAGE,
      RATE_LIMIT_EXCEEDED: RATE_LIMIT_MESSAGE,
      INVALID_ARGUMENT: INPUT_MESSAGE,
      INVALID_VALID: INPUT_MESSAGE,
      INVALID_VALIDATED: INPUT_MESSAGE,
    },
    fallback: '비밀번호 재설정에 실패했습니다. 링크를 확인하고 다시 시도해주세요.',
    network: '네트워크 연결을 확인하고 비밀번호 재설정을 다시 시도해주세요.',
    server: '비밀번호 재설정 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
  });
}

export function getEmailVerificationErrorMessage(error: unknown): string {
  return getSafeAuthErrorMessage(error, {
    codeMessages: {
      INVALID_TOKEN: '유효하지 않거나 만료된 인증 링크입니다.',
      RATE_LIMIT_EXCEEDED: RATE_LIMIT_MESSAGE,
    },
    fallback: '이메일 인증에 실패했습니다. 링크를 확인하고 다시 시도해주세요.',
    network: '네트워크 연결을 확인하고 이메일 인증을 다시 시도해주세요.',
    server: '이메일 인증 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
  });
}

export function getSocialLoginErrorMessage(error: unknown): string {
  return getSafeAuthErrorMessage(error, {
    codeMessages: {
      SOCIAL_AUTH_FAILED: '소셜 로그인에 실패했습니다. 다시 시도해주세요.',
      RATE_LIMIT_EXCEEDED: RATE_LIMIT_MESSAGE,
    },
    fallback: '소셜 로그인에 실패했습니다. 다시 시도해주세요.',
    network: '네트워크 연결을 확인하고 소셜 로그인을 다시 시도해주세요.',
    server: '소셜 로그인 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
  });
}
