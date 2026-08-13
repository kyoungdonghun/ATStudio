import { safeSessionStorage } from '@/utils/safeStorage';

const STORAGE_KEY = 'notice_create_observation_required';
const STORAGE_VALUE = '1';

let memoryFenceRequired = false;
let storedFenceMayExist = false;

export function isNoticeCreateObservationRequired(): boolean {
  const storedFenceExists = safeSessionStorage.getItem(STORAGE_KEY) === STORAGE_VALUE;
  if (storedFenceExists) storedFenceMayExist = true;
  return memoryFenceRequired || storedFenceExists;
}

export function requireNoticeCreateObservation(): void {
  memoryFenceRequired = true;
  if (safeSessionStorage.setItem(STORAGE_KEY, STORAGE_VALUE)) {
    storedFenceMayExist = true;
  }
}

export function clearNoticeCreateObservation(): void {
  const storedFenceExists = safeSessionStorage.getItem(STORAGE_KEY) === STORAGE_VALUE;
  if (storedFenceExists) storedFenceMayExist = true;
  if (!storedFenceMayExist) {
    memoryFenceRequired = false;
    return;
  }
  if (safeSessionStorage.removeItem(STORAGE_KEY)) {
    memoryFenceRequired = false;
    storedFenceMayExist = false;
  }
}
