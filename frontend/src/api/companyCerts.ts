import client from './client';

/* ── Types ── */

export interface CompanyCertification {
  id: number;
  status: string;       // PENDING | APPROVED | REJECTED
  adminNote: string | null;
  certificationCode: string | null;
  documentPath: string | null;
  approvedAt: string | null;
  createdAt: string;
}

/* ── API Functions ── */

/** 13.1 POST /api/company-certifications — apply (multipart) */
export async function applyCompanyCert(documents: File[]): Promise<CompanyCertification> {
  const form = new FormData();
  documents.forEach((file) => form.append('documents', file));
  const { data } = await client.post('/company-certifications', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data.data;
}

/** 13.2 GET /api/company-certifications/me — my status */
export async function fetchMyCompanyCert(): Promise<CompanyCertification> {
  const { data } = await client.get('/company-certifications/me');
  return data.data;
}
