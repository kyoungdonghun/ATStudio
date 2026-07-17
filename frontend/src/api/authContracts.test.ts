import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/api/client', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

import client from '@/api/client';
import {
  checkEmailAvailability,
  checkNicknameAvailability,
  checkPhoneAvailability,
  fetchMe,
  fetchPublicCapabilities,
  login,
  register,
  requestPasswordReset,
  resetPassword,
  socialLogin,
  verifyEmail,
} from '@/api/auth';

const mockedClient = vi.mocked(client);
const result = { id: 1, available: true, accessToken: 'access' };

describe('auth API contracts', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedClient.get.mockResolvedValue({ data: { data: result } });
    mockedClient.post.mockResolvedValue({ data: { data: result } });
  });

  it('submits password login and registration payloads', async () => {
    const credentials = { email: 'user@example.com', password: 'Password1!' };
    await expect(login(credentials)).resolves.toEqual(result);
    expect(mockedClient.post).toHaveBeenNthCalledWith(1, '/auth/login', credentials);

    const registration = {
      nickname: 'User',
      email: credentials.email,
      password: credentials.password,
      phonePersonal: '01012345678',
      phoneCompany: null,
      job: 'CREATOR',
      userType: 'INDIVIDUAL' as const,
    };
    await expect(register(registration)).resolves.toEqual(result);
    expect(mockedClient.post).toHaveBeenNthCalledWith(2, '/users', registration);
  });

  it('loads the current user with optional staged authorization', async () => {
    await fetchMe('staged-token');
    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/users/me', {
      headers: { Authorization: 'Bearer staged-token' },
    });
    await fetchMe();
    expect(mockedClient.get).toHaveBeenNthCalledWith(2, '/users/me', { headers: undefined });
  });

  it('checks all uniqueness endpoints', async () => {
    await checkEmailAvailability('user@example.com');
    await checkNicknameAvailability('User');
    await checkPhoneAvailability('01012345678');
    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/utils/check-email', {
      params: { email: 'user@example.com' },
    });
    expect(mockedClient.get).toHaveBeenNthCalledWith(2, '/utils/check-nickname', {
      params: { nickname: 'User' },
    });
    expect(mockedClient.get).toHaveBeenNthCalledWith(3, '/utils/check-phone', {
      params: { phone: '01012345678' },
    });
  });

  it('adds PKCE only when a verifier exists', async () => {
    await socialLogin('google', 'authorization-code', 'verifier');
    expect(mockedClient.post).toHaveBeenNthCalledWith(1, '/auth/social/google', {
      authorizationCode: 'authorization-code',
      codeVerifier: 'verifier',
    });
    await socialLogin('kakao', 'second-code', null);
    expect(mockedClient.post).toHaveBeenNthCalledWith(2, '/auth/social/kakao', {
      authorizationCode: 'second-code',
    });
  });

  it('uses password reset, email verification, and capability contracts', async () => {
    await requestPasswordReset({ email: 'user@example.com' });
    await resetPassword('reset-token', 'NewPassword1!');
    await verifyEmail('verify-token');
    await expect(fetchPublicCapabilities()).resolves.toEqual(result);
    expect(mockedClient.post).toHaveBeenNthCalledWith(1, '/auth/forgot-password', {
      email: 'user@example.com',
    });
    expect(mockedClient.post).toHaveBeenNthCalledWith(2, '/auth/reset-password', {
      token: 'reset-token',
      newPassword: 'NewPassword1!',
    });
    expect(mockedClient.get).toHaveBeenNthCalledWith(1, '/auth/verify-email', {
      params: { token: 'verify-token' },
    });
    expect(mockedClient.get).toHaveBeenNthCalledWith(2, '/utils/public-capabilities');
  });
});
