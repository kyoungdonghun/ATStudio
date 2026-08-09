import type { TagType } from '@/types';
import { TAG_NAME_MAX } from '@/utils/validation';

export const TAG_NAME_RAW_MAX = 200;
export const TAG_NAME_DUPLICATE_MESSAGE = '이미 존재하는 태그 이름입니다.';

const TAG_NAME_REQUIRED_MESSAGE = '태그 이름을 입력해주세요.';
const TAG_NAME_TOO_LONG_MESSAGE = `태그 이름은 ${TAG_NAME_MAX}자 이하로 입력해주세요.`;
const TAG_NAME_RAW_TOO_LONG_MESSAGE = `태그 이름 입력은 ${TAG_NAME_RAW_MAX}자 이하로 입력해주세요.`;
const TAG_NAME_CHARACTERS_MESSAGE = "한글, 영문, 숫자, 공백과 - & / ' ’ ( )만 사용할 수 있습니다.";
const EDGE_SPACE_SEPARATORS = /^\p{Zs}+|\p{Zs}+$/gu;
const SPACE_SEPARATOR_RUN = /\p{Zs}+/gu;
const DISALLOWED_TAG_NAME = /[^\p{Script=Hangul}A-Za-z0-9 &/'\u2019()-]/u;

interface TagNameEntry {
  id: number;
  name: string;
}

export function normalizeTagName(rawName: string): string {
  return rawName
    .replace(EDGE_SPACE_SEPARATORS, '')
    .replace(SPACE_SEPARATOR_RUN, ' ')
    .normalize('NFC');
}

export function getTagNameValidationError(rawName: string): string | null {
  if (Array.from(rawName).length > TAG_NAME_RAW_MAX) return TAG_NAME_RAW_TOO_LONG_MESSAGE;

  const canonicalName = normalizeTagName(rawName);
  if (!canonicalName) return TAG_NAME_REQUIRED_MESSAGE;
  if (Array.from(canonicalName).length > TAG_NAME_MAX) return TAG_NAME_TOO_LONG_MESSAGE;
  if (DISALLOWED_TAG_NAME.test(canonicalName)) return TAG_NAME_CHARACTERS_MESSAGE;
  return null;
}

export function isDuplicateTagName(
  tags: TagNameEntry[],
  rawName: string,
  excludedTagId?: number,
): boolean {
  const canonicalName = normalizeTagName(rawName);
  return tags.some(
    (tag) => tag.id !== excludedTagId && normalizeTagName(tag.name) === canonicalName,
  );
}

export function formatTagNameForDisplay(name: string, type: TagType): string {
  return type === 'USAGE' ? `#${name}` : name;
}
