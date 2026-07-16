import client from './client';
import type { ApiResponse, CompanyCertification } from '@/types';

export function getCompanyCertErrorStatus(error: unknown): number | undefined {
  if (!error || typeof error !== 'object' || !('response' in error)) return undefined;
  return (error as { response?: { status?: number } }).response?.status;
}

export function getCompanyCertErrorMessage(error: unknown, fallback: string): string {
  const status = getCompanyCertErrorStatus(error);
  if (status === 403) {
    return '기업 회원만 기업 인증을 이용할 수 있습니다. 계정 유형을 확인해주세요.';
  }

  const message =
    error && typeof error === 'object' && 'response' in error
      ? (error as { response?: { data?: { message?: unknown } } }).response?.data?.message
      : undefined;
  if (typeof message === 'string' && message.trim()) return message.trim();

  if (status === 400 || status === 422) {
    return '서류 형식, 개수, 용량을 확인한 뒤 다시 시도해주세요.';
  }
  return fallback;
}

/* ── API Functions ── */

/** 13.1 POST /api/company-certifications — apply (multipart) */
export async function applyCompanyCert(documents: File[]): Promise<CompanyCertification> {
  const form = new FormData();
  documents.forEach((file) => form.append('documents', file));
  const { data } = await client.post<ApiResponse<CompanyCertification>>(
    '/company-certifications',
    form,
    {
      timeout: 60_000, // 1 minute for document upload
    },
  );
  return data.data;
}

/** 13.2 POST /api/company-certifications/me/documents — resubmit after revision request */
export async function resubmitCompanyCert(documents: File[]): Promise<CompanyCertification> {
  const form = new FormData();
  documents.forEach((file) => form.append('documents', file));
  const { data } = await client.post<ApiResponse<CompanyCertification>>(
    '/company-certifications/me/documents',
    form,
    {
      timeout: 60_000,
    },
  );
  return data.data;
}

/** 13.3 GET /api/company-certifications/me — my status */
export async function fetchMyCompanyCert(): Promise<CompanyCertification> {
  const { data } = await client.get<ApiResponse<CompanyCertification>>(
    '/company-certifications/me',
  );
  return data.data;
}
