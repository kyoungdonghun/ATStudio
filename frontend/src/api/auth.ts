import client from '@/api/client';
import type { ApiResponse } from '@/types';

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
  job: string;
  userType: 'INDIVIDUAL' | 'BUSINESS';
}

export interface PasswordResetRequest {
  email: string;
}

export interface VerifyEmailRequest {
  token: string;
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
  phonePersonal: string;
  phoneCompany: string | null;
  job: string;
  userType: string;
  role: 'USER' | 'CREATOR' | 'ADMIN';
  isVerified: boolean;
  createdAt: string;
}

export interface CheckAvailabilityResponse {
  available: boolean;
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

/**
 * Password reset request (placeholder).
 */
export function requestPasswordReset(_data: PasswordResetRequest) {
  return Promise.resolve({ message: 'ok' });
}

/**
 * Email verification (placeholder).
 */
export function verifyEmail(_data: VerifyEmailRequest) {
  return Promise.resolve({ message: 'ok' });
}
