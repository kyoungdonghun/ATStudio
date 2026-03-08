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

export function login(data: LoginRequest) {
  return client.post<LoginResponse>('/auth/login', data);
}

export function register(data: RegisterRequest) {
  return client.post<ApiResponse<RegisterResponse>>('/users', data);
}

export function fetchMe() {
  return client.get<MeResponse>('/users/me');
}

export function checkEmailAvailability(email: string) {
  return client.get<CheckAvailabilityResponse>('/utils/check-email', {
    params: { email },
  });
}

export function checkNicknameAvailability(nickname: string) {
  return client.get<CheckAvailabilityResponse>('/utils/check-nickname', {
    params: { nickname },
  });
}

/**
 * Password reset request (placeholder).
 * The backend API for this endpoint is not yet implemented.
 * This function simulates a successful response for frontend flow development.
 */
export function requestPasswordReset(_data: PasswordResetRequest) {
  return Promise.resolve({ data: { message: 'ok' } });
}

/**
 * Email verification (placeholder).
 * The backend API for this endpoint is not yet implemented.
 * This function simulates a successful response for frontend flow development.
 */
export function verifyEmail(_data: VerifyEmailRequest) {
  return Promise.resolve({ data: { message: 'ok' } });
}
