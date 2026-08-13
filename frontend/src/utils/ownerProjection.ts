import { useAuthStore } from '@/store/authStore';

export type OwnerProjectionKey = string;

export function createOwnerKey(
  userID: number | null,
  accessToken: string | null,
): OwnerProjectionKey | null {
  if (userID === null || accessToken === null) return null;
  return JSON.stringify([userID, accessToken]);
}

export function createReadKey(
  ownerKey: OwnerProjectionKey | null,
  ...parts: ReadonlyArray<string | number | boolean | null>
): OwnerProjectionKey | null {
  if (ownerKey === null) return null;
  return JSON.stringify([ownerKey, ...parts]);
}

export function getCurrentOwnerKey(
  fallbackOwnerKey: OwnerProjectionKey | null = null,
): OwnerProjectionKey | null {
  const getState = useAuthStore.getState;
  if (typeof getState !== 'function') return fallbackOwnerKey;
  const { user, accessToken } = getState();
  return createOwnerKey(user?.id ?? null, accessToken);
}
