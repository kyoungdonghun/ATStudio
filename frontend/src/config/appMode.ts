/** Build-time presentation mode. Backend policy must be enabled separately. */
export const isListeningMode = import.meta.env.VITE_APP_MODE === 'listening';

export function isListeningOperatorPath(pathname: string): boolean {
  return pathname === '/login' || pathname === '/admin' || pathname.startsWith('/admin/');
}

export function isListeningVisitor(): boolean {
  return isListeningMode && !isListeningOperatorPath(window.location.pathname);
}
