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
  companyName: string | null;
  userType: UserType;
  isVerified: boolean;
  createdAt: string;
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
  likeCount: number;
  downloadCount: number;
  waveformData?: string | null;
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
  waveformData?: string | null;
  tags: TagItem[];
  isActive: boolean;
  playCount: number;
  likeCount: number;
  downloadCount: number;
  createdAt: string;
  updatedAt: string;
}

/* ── Tag ── */

export type TagType = 'GENRE' | 'MOOD' | 'INSTRUMENT' | 'USAGE';

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
  likeCount: number;
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
  maxPlaylists: number;
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
  viewCount: number;
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

/* ── Legacy Download Queue (pre-SR-79) ── */

/** @deprecated Legacy queue item. Subscriber-facing page uses download history items. */
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

export interface AlbumLikeItem {
  albumId: number;
  title: string;
  description: string | null;
  thumbnailUrl: string | null;
  trackCount: number;
  likeCount: number;
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

export type CertificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'REVISION_REQUESTED';

export interface CompanyCertificationDocument {
  id: number;
  originalFilename: string;
  contentType: string | null;
  sizeBytes: number;
  createdAt: string;
}

/** Detail response (GET /company-certifications/me, GET /company-certifications/{id}) */
export interface CompanyCertification {
  id: number;
  userId: number;
  userNickname: string;
  userEmail: string;
  companyName: string | null;
  phoneCompany: string | null;
  status: CertificationStatus;
  adminNote: string | null;
  documentPath: string | null;
  documents: CompanyCertificationDocument[];
  certificationCode: string | null;
  approvedAt: string | null;
  createdAt: string;
}

/** Summary response for admin list (GET /company-certifications) */
export interface CompanyCertificationSummary {
  id: number;
  userId: number;
  userNickname: string;
  userEmail: string;
  companyName: string | null;
  status: CertificationStatus;
  createdAt: string;
}

/* ── Whitelist Channel ── */

export type WhitelistChannelStatus =
  | 'DRAFT'
  | 'PENDING'
  | 'EXPORTED'
  | 'REGISTERED'
  | 'REVISION_REQUESTED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'REMOVAL_REQUESTED';

export interface WhitelistChannel {
  id: number;
  channelUrl: string;
  channelName: string;
  youtubeHandle: string | null;
  youtubeChannelId: string | null;
  status: WhitelistChannelStatus;
  primary: boolean;
  adminNote: string | null;
  requestedAt: string | null;
  exportedAt: string | null;
  processedAt: string | null;
  removalRequestedAt: string | null;
  createdAt: string;
}
