import type { User } from '@/types';
import { safeStorage } from '@/utils/safeStorage';

const ACTIVE_SESSION = 'atstudio.pilot.session';
export const PILOT_SESSION_CHANGED = 'atstudio:pilot-session-changed';
const recordKey = (id: string) => `${ACTIVE_SESSION}.${id}`;
const activeKey = (id: string) => `${recordKey(id)}.active`;

export interface PilotSession {
  id: string;
  user: User;
  accessToken: string;
  refreshToken: string | null;
}

// Login alone selects a new session. A delayed refresh/logout can only change
// its own record, even if another tab selects a new login between storage calls.
export function readPilotSession(): PilotSession | null {
  const id = safeStorage.getItem(ACTIVE_SESSION);
  if (!id || !safeStorage.getItem(activeKey(id))) return null;
  try {
    const raw = safeStorage.getItem(recordKey(id));
    if (!raw) return null;
    const session: PilotSession = JSON.parse(raw);
    if (
      session.id !== id ||
      !session.accessToken ||
      !Number.isSafeInteger(session.user?.id) ||
      !['ADMIN', 'USER'].includes(session.user.role)
    )
      return null;
    return session;
  } catch {
    return null;
  }
}

export function savePilotSession(session: PilotSession): void {
  if (!safeStorage.setItem(recordKey(session.id), JSON.stringify(session))) {
    throw new Error('Failed to persist pilot session');
  }
}

export function removePilotSession(id: string): void {
  safeStorage.removeItem(activeKey(id));
  safeStorage.removeItem(recordKey(id));
}

export function startPilotSession(
  user: User,
  accessToken: string,
  refreshToken: string | null,
): PilotSession {
  const previous = safeStorage.getItem(ACTIVE_SESSION);
  const session = { id: crypto.randomUUID(), user, accessToken, refreshToken };
  savePilotSession(session);
  if (
    !safeStorage.setItem(activeKey(session.id), '1') ||
    !safeStorage.setItem(ACTIVE_SESSION, session.id)
  ) {
    removePilotSession(session.id);
    throw new Error('Failed to select pilot session');
  }
  if (previous) removePilotSession(previous);
  return session;
}

export function isPilotSessionStorageKey(key: string | null): boolean {
  return key === null || key === ACTIVE_SESSION || key.startsWith(ACTIVE_SESSION + '.');
}
