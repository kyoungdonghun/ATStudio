import client from '@/api/client';
import type {
  ApiResponse,
  PagedResponse,
  User,
  CompanyCertification,
  CompanyCertificationSummary,
  CertificationStatus,
  UserRole,
} from '@/types';

/* ── Dashboard Stats ── */

export interface DashboardStats {
  totalUsers: number;
  totalTracks: number;
  totalSubscribers: number;
  recentUsers: User[];
}

/**
 * Aggregates dashboard stats from multiple admin endpoints.
 * There is no single stats API -- we combine user list + track list data.
 */
export async function fetchDashboardStats(): Promise<DashboardStats> {
  const [usersRes, tracksRes] = await Promise.all([
    client.get<PagedResponse<User>>('/users', { params: { page: 1, size: 5 } }),
    client.get<PagedResponse<{ id: number }>>('/tracks/admin', {
      params: { page: 1, size: 1 },
    }),
  ]);

  const userPage = usersRes.data;
  const trackPage = tracksRes.data;

  return {
    totalUsers: userPage.pageInfo.total,
    totalTracks: trackPage.pageInfo.total,
    totalSubscribers: 0,
    recentUsers: userPage.dataList,
  };
}

/* ── User Management ── */

interface UserListParams {
  page?: number;
  size?: number;
  keyword?: string;
  userType?: string;
}

export async function fetchUsers(
  params: UserListParams = {},
): Promise<PagedResponse<User>> {
  const { data } = await client.get<PagedResponse<User>>('/users', { params });
  return data;
}

export async function fetchUser(userId: number): Promise<User> {
  const { data } = await client.get<ApiResponse<User>>(`/users/${userId}`);
  return data.data;
}

interface UpdateUserAdminRequest {
  role?: UserRole;
  isVerified?: boolean;
}

export async function updateUserAdmin(
  userId: number,
  body: UpdateUserAdminRequest,
): Promise<User> {
  const { data } = await client.put<ApiResponse<User>>(
    `/users/${userId}`,
    body,
  );
  return data.data;
}

/* ── Company Certification ── */

interface CertListParams {
  page?: number;
  size?: number;
  status?: CertificationStatus;
}

export async function fetchCompanyCerts(
  params: CertListParams = {},
): Promise<PagedResponse<CompanyCertificationSummary>> {
  const { data } = await client.get<PagedResponse<CompanyCertificationSummary>>(
    '/company-certifications',
    { params },
  );
  return data;
}

export async function fetchCompanyCert(
  certId: number,
): Promise<CompanyCertification> {
  const { data } = await client.get<ApiResponse<CompanyCertification>>(
    `/company-certifications/${certId}`,
  );
  return data.data;
}

interface ProcessCertRequest {
  status: CertificationStatus;
  adminNote?: string;
}

export interface ProcessCertResponse {
  id: number;
  status: CertificationStatus;
  certificationCode: string | null;
  approvedAt: string | null;
}

export async function processCompanyCert(
  certId: number,
  body: ProcessCertRequest,
): Promise<ProcessCertResponse> {
  const { data } = await client.put<ApiResponse<ProcessCertResponse>>(
    `/company-certifications/${certId}`,
    body,
  );
  return data.data;
}
