import client from '@/api/client';
import type { ApiResponse, PagedResponse } from '@/types';

/* ── Response types ── */

export interface LicenseTrack {
  id: number;
  title: string;
  bpm?: number;
  tonality?: string;
}

export interface LicenseListItem {
  id: number;
  track: LicenseTrack;
  licenseCode: string;
  issuedAt: string;
}

export interface LicenseDetail {
  id: number;
  track: LicenseTrack;
  licenseCode: string;
  issuedAt: string;
  user: { id: number; nickname: string };
}

/* ── API functions ── */

/** GET /api/users/{userId}/licenses -- admin: licenses by user (paginated) */
export async function fetchUserLicenses(
  userId: number,
  page = 1,
  size = 20,
): Promise<PagedResponse<LicenseListItem>> {
  const { data } = await client.get<PagedResponse<LicenseListItem>>(`/users/${userId}/licenses`, {
    params: { page, size },
  });
  return data;
}

/** GET /api/licenses/me -- my licenses (paginated) */
export async function fetchMyLicenses(
  page = 1,
  size = 20,
): Promise<PagedResponse<LicenseListItem>> {
  const { data } = await client.get<PagedResponse<LicenseListItem>>('/licenses/me', {
    params: { page, size },
  });
  return data;
}

/** GET /api/licenses/{licenseId} -- license detail */
export async function fetchLicenseDetail(licenseId: number): Promise<LicenseDetail> {
  const { data } = await client.get<ApiResponse<LicenseDetail>>(`/licenses/${licenseId}`);
  return data.data;
}
