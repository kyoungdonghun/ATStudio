/* ──────────────────────────────────────────────
 * ATStudio Common TypeScript Types
 * Based on api-spec.md response structures
 * ────────────────────────────────────────────── */

/** Generic API wrapper returned by most endpoints */
export interface ApiResponse<T> {
  message: string;
  data: T;
}

/** Pagination metadata returned by the backend */
export interface PageInfo {
  page: number;
  size: number;
  total: number;
  start: number;
  end: number;
  prev: boolean;
  next: boolean;
}

/** Paginated API wrapper matching backend response */
export interface PagedResponse<T> {
  dataList: T[];
  pageInfo: PageInfo;
}

/* ── Auth ── */

/**
 * Frontend-extended role type.
 * Backend Java enum has USER | ADMIN only.
 * GUEST is a frontend-only value representing unauthenticated users.
 */
export type UserRole = 'GUEST' | 'USER' | 'ADMIN';

export type UserJob = 'EDITOR' | 'ARTIST' | 'FREELANCER';

export type UserType = 'INDIVIDUAL' | 'BUSINESS';

export interface User {
  id: number;
  email: string;
  nickname: string;
  role: UserRole;
  phonePersonal: string | null;
  phoneCompany: string | null;
  job: UserJob | null;
  userType: UserType;
  isVerified: boolean;
  createdAt: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

/* ── Track ── */

/** Track list item (from GET /api/tracks) */
export interface TrackListItem {
  id: number;
  title: string;
  artistName: string;
  duration: number;
  bpm: number;
  tonality: string;
  thumbnail: string | null;
  playCount: number;
  tags: TagItem[];
  createdAt: string;
}

/** Track detail (from GET /api/tracks/{id}) */
export interface Track {
  id: number;
  title: string;
  artistName: string;
  duration: number;
  bpm: number;
  tonality: string;
  description: string | null;
  audioFile: string | null;
  thumbnail: string | null;
  tags: TagItem[];
  isActive: boolean;
  playCount: number;
  createdAt: string;
  updatedAt: string;
}

/* ── Tag ── */

export type TagType = 'GENRE' | 'MOOD' | 'INSTRUMENT';

export interface TagItem {
  id: number;
  name: string;
  type: TagType;
}

/* ── Album ── */

/** Album list item (from GET /api/albums) */
export interface Album {
  id: number;
  title: string;
  description: string | null;
  thumbnailUrl: string | null;
  trackCount: number;
  createdAt: string;
}

/* ── Playlist ── */

export interface Playlist {
  id: number;
  title: string;
  description: string | null;
  thumbnail: string | null;
  trackCount: number;
  createdAt: string;
}

/* ── Subscription ── */

export interface Subscription {
  id: number;
  name: string;
  description: string | null;
  userType: UserType;
  priceMonthly: number;
  priceYearly: number;
  downloadPerDay: number;
  maxWhitelistChannels: number;
  isActive: boolean;
}

export interface UserSubscription {
  id: number;
  subscription: Subscription;
  billingCycle: 'MONTHLY' | 'YEARLY';
  status: 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
  startedAt: string;
  expiresAt: string | null;
  pendingSubscriptionId: number | null;
  pendingBillingCycle: 'MONTHLY' | 'YEARLY' | null;
}

/* ── License ── */

export interface License {
  id: number;
  track: { id: number; title: string };
  licenseCode: string;
  issuedAt: string;
}

/* ── Notice ── */

export interface NoticeAttachmentInfo {
  id: number;
  originalName: string;
  fileSize: number;
}

export interface Notice {
  id: number;
  title: string;
  content: string;
  isPinned: boolean;
  attachments?: NoticeAttachmentInfo[] | null;
  createdAt: string;
  updatedAt: string;
}

/* ── Play History ── */

export interface PlayHistory {
  id: number;
  track: { id: number; title: string; artistName: string; thumbnail: string | null };
  playedAt: string;
}

/* ── Download Queue ── */

export interface DownloadQueueItem {
  trackId: number;
  title: string;
  bpm: number;
  tonality: string;
  thumbnail: string | null;
  createdAt: string;
}

/* ── Like ── */

export interface LikeItem {
  trackId: number;
  title: string;
  bpm: number;
  tonality: string;
  thumbnail: string | null;
  createdAt: string;
}

/* ── Question ── */

export type QuestionCategory = 'DOWNLOAD' | 'PAYMENT' | 'COPYRIGHT' | 'PRODUCTION' | 'OTHER';

export type QuestionStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';

export interface Question {
  id: number;
  title: string;
  content: string;
  category: QuestionCategory;
  isPublic: boolean;
  status: QuestionStatus;
  createdAt: string;
}

/* ── Company Certification ── */

export type CertificationStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'REVISION_REQUESTED';

/** Detail response (GET /company-certifications/me, GET /company-certifications/{id}) */
export interface CompanyCertification {
  id: number;
  status: CertificationStatus;
  adminNote: string | null;
  documentPath: string | null;
  certificationCode: string | null;
  approvedAt: string | null;
  createdAt: string;
}

/** Summary response for admin list (GET /company-certifications) */
export interface CompanyCertificationSummary {
  id: number;
  userId: number;
  userNickname: string;
  status: CertificationStatus;
  createdAt: string;
}

/* ── Whitelist Channel ── */

export interface WhitelistChannel {
  id: number;
  channelUrl: string;
  channelName: string;
  createdAt: string;
}
