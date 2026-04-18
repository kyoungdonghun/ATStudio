import client from '@/api/client';
import type { ApiResponse, UserJob, UserRole, UserType } from '@/types';

/* ── Request Types ── */

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nickname: string;
  email: string;
  password: string;
  phonePersonal: string;
  phoneCompany: string | null;
  job: string | null;
  companyName?: string;
  userType: 'INDIVIDUAL' | 'BUSINESS';
}

export interface PasswordResetRequest {
  email: string;
}

/* ── Response Types ── */

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface RegisterResponse {
  id: number;
  nickname: string;
  email: string;
  job: string;
  userType: string;
  isVerified: boolean;
  createdAt: string;
}

export interface MeResponse {
  id: number;
  nickname: string;
  email: string;
  phonePersonal: string | null;
  phoneCompany: string | null;
  job: UserJob | null;
  companyName: string | null;
  userType: UserType;
  role: Exclude<UserRole, 'GUEST'>;
  isVerified: boolean;
  createdAt: string;
}

export interface CheckAvailabilityResponse {
  available: boolean;
}

export interface OAuthProviderCapability {
  enabled: boolean;
  clientId: string | null;
  redirectUri: string | null;
}

export interface PublicCapabilitiesResponse {
  passwordLoginEnabled: boolean;
  emailVerification: {
    enabled: boolean;
    deliveryMode: 'UNCONFIGURED' | 'LOCAL_SMTP' | 'REMOTE_SMTP';
  };
  passwordReset: {
    enabled: boolean;
    deliveryMode: 'UNCONFIGURED' | 'LOCAL_SMTP' | 'REMOTE_SMTP';
  };
  socialLogin: {
    google: OAuthProviderCapability;
    kakao: OAuthProviderCapability;
    naver: OAuthProviderCapability;
  };
  testUsersEnabled: boolean;
}

/* ── API Functions ── */

export async function login(req: LoginRequest): Promise<LoginResponse> {
  const { data } = await client.post<ApiResponse<LoginResponse>>('/auth/login', req);
  return data.data;
}

export async function register(data: RegisterRequest): Promise<RegisterResponse> {
  const res = await client.post<ApiResponse<RegisterResponse>>('/users', data);
  return res.data.data;
}

export async function fetchMe(): Promise<MeResponse> {
  const { data } = await client.get<ApiResponse<MeResponse>>('/users/me');
  return data.data;
}

export async function checkEmailAvailability(email: string): Promise<CheckAvailabilityResponse> {
  const { data } = await client.get<ApiResponse<CheckAvailabilityResponse>>('/utils/check-email', {
    params: { email },
  });
  return data.data;
}

export async function checkNicknameAvailability(nickname: string): Promise<CheckAvailabilityResponse> {
  const { data } = await client.get<ApiResponse<CheckAvailabilityResponse>>('/utils/check-nickname', {
    params: { nickname },
  });
  return data.data;
}

/** POST /api/auth/social/{provider} -- social login with PKCE */
export async function socialLogin(
  provider: string,
  authorizationCode: string,
  codeVerifier?: string | null,
): Promise<LoginResponse & { isProfileComplete: boolean }> {
  const body: Record<string, string> = { authorizationCode };
  if (codeVerifier) body.codeVerifier = codeVerifier;
  const { data } = await client.post<
    ApiResponse<LoginResponse & { isProfileComplete: boolean }>
  >(`/auth/social/${provider}`, body);
  return data.data;
}

/** GET /api/utils/check-phone -- phone availability check */
export async function checkPhoneAvailability(
  phone: string,
): Promise<CheckAvailabilityResponse> {
  const { data } = await client.get<ApiResponse<CheckAvailabilityResponse>>(
    '/utils/check-phone',
    { params: { phone } },
  );
  return data.data;
}

/** POST /api/auth/forgot-password */
export async function requestPasswordReset(req: PasswordResetRequest): Promise<void> {
  await client.post('/auth/forgot-password', req);
}

/** POST /api/auth/reset-password */
export async function resetPassword(token: string, newPassword: string): Promise<void> {
  await client.post('/auth/reset-password', { token, newPassword });
}

/** GET /api/auth/verify-email?token=... */
export async function verifyEmail(token: string): Promise<void> {
  await client.get('/auth/verify-email', { params: { token } });
}

/** GET /api/utils/public-capabilities -- public auth/runtime capability hints */
export async function fetchPublicCapabilities(): Promise<PublicCapabilitiesResponse> {
  const { data } = await client.get<ApiResponse<PublicCapabilitiesResponse>>(
    '/utils/public-capabilities',
  );
  return data.data;
}
