/** Centralized validation rules — single source of truth for frontend */

// ── User ──
export const NICKNAME_MIN = 2;
export const NICKNAME_MAX = 20;
export const NICKNAME_PATTERN = /^[가-힣a-zA-Z0-9_]+$/;

export const PASSWORD_MIN = 8;
export const PASSWORD_MAX = 100;

export const PHONE_PATTERN = /^\d{2,3}-\d{3,4}-\d{4}$/;
export const PHONE_MAX_DIGITS = 11;

export const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const COMPANY_NAME_MAX = 100;

// ── Content ──
export const TITLE_TRACK_MAX = 100;
export const TITLE_ALBUM_MAX = 100;
export const TITLE_PLAYLIST_MAX = 50;
export const TITLE_NOTICE_MAX = 200;
export const TITLE_QUESTION_MAX = 200;

export const DESCRIPTION_MAX = 1000;

export const TAG_NAME_MAX = 50;

export const BPM_MIN = 1;
export const BPM_MAX = 999;

// ── File ──
export const AUDIO_MAX_SIZE_MB = 30;
export const IMAGE_MAX_SIZE_MB = 10;
export const ATTACHMENT_MAX_SIZE_MB = 20;
export const ATTACHMENT_MAX_COUNT = 5;
export const TRACK_UPLOAD_MAX_COUNT = 20;

// ── Company Certification ──
export const CERT_DOC_ACCEPT = '.pdf,.hwp,.hwpx,.doc,.docx,.jpg,.jpeg,.png';
export const CERT_DOC_EXTENSIONS = ['pdf', 'hwp', 'hwpx', 'doc', 'docx', 'jpg', 'jpeg', 'png'];
export const CERT_DOC_MAX_SIZE_MB = 20;
export const CERT_DOC_MAX_COUNT = 10;
export const CERT_DOC_LABEL = 'PDF, HWP, DOCX, JPG, PNG';

// ── Search ──
export const SEARCH_KEYWORD_MAX = 100;

// ── Whitelist Channel ──
export const CHANNEL_NAME_MAX = 100;
export const CHANNEL_URL_PATTERN = /^https?:\/\/.+/;

// ── Helpers ──

/** 숫자만 추출 후 전화번호 포맷 적용 (010-1234-5678, 02-123-4567) */
export function formatPhone(value: string): string {
  const digits = value.replace(/\D/g, '').slice(0, PHONE_MAX_DIGITS);
  if (digits.startsWith('02')) {
    if (digits.length <= 2) return digits;
    if (digits.length <= 5) return `${digits.slice(0, 2)}-${digits.slice(2)}`;
    if (digits.length <= 9) return `${digits.slice(0, 2)}-${digits.slice(2, 5)}-${digits.slice(5)}`;
    return `${digits.slice(0, 2)}-${digits.slice(2, 6)}-${digits.slice(6)}`;
  }
  if (digits.length <= 3) return digits;
  if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
  return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
}

export function isValidEmail(v: string): boolean {
  return EMAIL_PATTERN.test(v);
}

export function isValidPhone(v: string): boolean {
  return PHONE_PATTERN.test(v);
}

export function isValidNickname(v: string): boolean {
  return v.length >= NICKNAME_MIN && v.length <= NICKNAME_MAX && NICKNAME_PATTERN.test(v);
}

export function isValidPassword(v: string): boolean {
  return v.length >= PASSWORD_MIN && v.length <= PASSWORD_MAX;
}

export function isFileSizeOk(file: File, maxMb: number): boolean {
  return file.size <= maxMb * 1024 * 1024;
}

/** Validate image dimensions: min 200x200, aspect ratio within 1:3 ~ 3:1 */
export function validateImageDimensions(file: File): Promise<string | null> {
  return new Promise((resolve) => {
    const img = new window.Image();
    img.onload = () => {
      URL.revokeObjectURL(img.src);
      if (img.width < 200 || img.height < 200) {
        resolve('이미지는 최소 200x200px 이상이어야 합니다.');
        return;
      }
      const ratio = img.width / img.height;
      if (ratio > 3 || ratio < 1 / 3) {
        resolve('이미지 비율이 너무 극단적입니다. 정사각형에 가까운 이미지를 사용해주세요.');
        return;
      }
      resolve(null);
    };
    img.onerror = () => {
      URL.revokeObjectURL(img.src);
      resolve('이미지 파일을 읽을 수 없습니다.');
    };
    img.src = URL.createObjectURL(file);
  });
}
