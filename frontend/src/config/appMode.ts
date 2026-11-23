import { isPilotMode } from '@/config/pilotMode';
/** Build-time presentation mode. Backend policy must be enabled separately. */
export const isListeningMode = import.meta.env.VITE_APP_MODE === 'listening' || isPilotMode;

export function isListeningOperatorPath(pathname: string): boolean {
  return pathname === '/login' || pathname === '/admin' || pathname.startsWith('/admin/');
}

export function isListeningVisitor(method?: string, url?: string): boolean {
  if (isPilotMode) {
    const path = url?.split('?')[0] ?? '';
    const verb = method?.toLowerCase();
    if (
      verb === 'get' &&
      /^\/(?:tracks(?:\/[1-9]\d*(?:\/stream)?)?|tags(?:\/available)?)$/.test(path)
    )
      return true;
    if (verb === 'post' && path === '/tracks/batch') return true;
    if (
      verb === 'get' &&
      /^\/(tracks\/[1-9]\d*\/download|licenses\/(me|[1-9]\d*)|downloads\/history|utils\/download-count|users\/me)$/.test(
        path,
      )
    )
      return false;
    if (verb === 'post' && path === '/auth/logout') return false;
  }
  return isListeningMode && !isListeningOperatorPath(window.location.pathname);
}
