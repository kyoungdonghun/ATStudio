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

export type UserRole = 'GUEST' | 'USER' | 'CREATOR' | 'ADMIN';

export interface User {
  id: number;
  email: string;
  name: string;
  nickname: string;
  role: UserRole;
  profileImageUrl: string | null;
  provider: string;
  createdAt: string;
  updatedAt: string;
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
  name: string;
  description: string | null;
  coverImageUrl: string | null;
  trackCount: number;
  isPublic: boolean;
  createdAt: string;
  updatedAt: string;
}

/* ── Subscription ── */

export type SubscriptionTier = 'FREE' | 'BASIC' | 'PRO' | 'PREMIUM';

export interface Subscription {
  id: number;
  name: string;
  tier: SubscriptionTier;
  price: number;
  dailyDownloadLimit: number;
  description: string | null;
}

export interface UserSubscription {
  id: number;
  subscription: Subscription;
  status: 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
  startDate: string;
  endDate: string;
  expiresAt: string | null;
  createdAt: string;
}

/* ── License ── */

export interface License {
  id: number;
  trackId: number;
  trackTitle: string;
  licenseType: string;
  issuedAt: string;
  expiresAt: string | null;
}

/* ── Notice ── */

export interface Notice {
  id: number;
  title: string;
  content: string;
  isPinned: boolean;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

/* ── Play History ── */

export interface PlayHistory {
  id: number;
  track: Track;
  playedAt: string;
}

/* ── Download Queue ── */

export interface DownloadQueueItem {
  trackId: number;
  track: Track;
  addedAt: string;
}

/* ── Like ── */

export interface LikeItem {
  trackId: number;
  track: Track;
  createdAt: string;
}

/* ── Question ── */

export interface Question {
  id: number;
  title: string;
  content: string;
  status: 'OPEN' | 'ANSWERED' | 'CLOSED';
  createdAt: string;
  updatedAt: string;
}

/* ── Company Certification ── */

export type CertificationStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'REVISION_REQUESTED';

export interface CompanyCertification {
  id: number;
  companyName: string;
  businessNumber: string;
  status: CertificationStatus;
  createdAt: string;
  updatedAt: string;
}

/* ── Whitelist Channel ── */

export interface WhitelistChannel {
  id: number;
  channelUrl: string;
  channelName: string;
  platform: string;
  createdAt: string;
}
