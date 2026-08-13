import { describe, expect, it } from 'vitest';
import {
  getCompleteProfileErrorMessage,
  getEmailVerificationErrorMessage,
  getForgotPasswordErrorMessage,
  getPasswordResetErrorMessage,
  getPasswordUpdateErrorMessage,
  getProfileUpdateErrorMessage,
  getSignupErrorMessage,
  getSocialLoginErrorMessage,
} from '@/api/authError';

function apiError(errorCode: string, status: number, message = 'unsafe backend detail') {
  return { response: { status, data: { errorCode, message } } };
}

describe('auth error presentation', () => {
  it('maps forgot-password failures without exposing account or backend details', () => {
    expect(getForgotPasswordErrorMessage(apiError('RATE_LIMIT_EXCEEDED', 429))).toBe(
      '짧은 시간에 요청이 너무 많았습니다. 잠시 후 다시 시도해주세요.',
    );
    expect(getForgotPasswordErrorMessage(apiError('EMAIL_NOT_FOUND', 404))).toBe(
      '비밀번호 재설정 요청에 실패했습니다. 잠시 후 다시 시도해주세요.',
    );
    expect(getForgotPasswordErrorMessage(apiError('INTERNAL_ERROR', 500))).toBe(
      '서버 오류로 요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.',
    );
  });

  it('allows only bounded password-update guidance', () => {
    expect(getPasswordUpdateErrorMessage(apiError('INVALID_CREDENTIALS', 401))).toBe(
      '현재 비밀번호가 올바르지 않습니다.',
    );
    expect(getPasswordUpdateErrorMessage(apiError('UNEXPECTED', 400))).toBe(
      '비밀번호 변경에 실패했습니다. 입력값을 확인하고 다시 시도해주세요.',
    );
  });

  it('uses fixed mappings for adjacent auth mutations and never returns raw messages', () => {
    const unsafe = 'stack trace with private provider payload';

    expect(getSignupErrorMessage(apiError('EMAIL_ALREADY_REGISTERED', 409, unsafe))).toBe(
      '이미 가입된 이메일입니다.',
    );
    expect(getCompleteProfileErrorMessage(apiError('PHONE_ALREADY_REGISTERED', 409, unsafe))).toBe(
      '이미 등록된 전화번호입니다.',
    );
    expect(getProfileUpdateErrorMessage(apiError('NICKNAME_DUPLICATED', 409, unsafe))).toBe(
      '이미 사용 중인 닉네임입니다.',
    );
    expect(getPasswordResetErrorMessage(apiError('INVALID_TOKEN', 400, unsafe))).toBe(
      '유효하지 않거나 만료된 재설정 링크입니다.',
    );
    expect(getEmailVerificationErrorMessage(apiError('INVALID_TOKEN', 400, unsafe))).toBe(
      '유효하지 않거나 만료된 인증 링크입니다.',
    );
    expect(getSocialLoginErrorMessage(apiError('SOCIAL_AUTH_FAILED', 401, unsafe))).toBe(
      '소셜 로그인에 실패했습니다. 다시 시도해주세요.',
    );
  });

  it.each(['__proto__', 'constructor'])(
    'treats the adversarial %s error code as unmapped bounded input',
    (errorCode) => {
      const result = getEmailVerificationErrorMessage(apiError(errorCode, 400));

      expect(result).toBe('이메일 인증에 실패했습니다. 링크를 확인하고 다시 시도해주세요.');
      expect(typeof result).toBe('string');
    },
  );
});
