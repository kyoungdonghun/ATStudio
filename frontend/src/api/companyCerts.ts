import client from './client';
import type { ApiResponse, CompanyCertification } from '@/types';

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
