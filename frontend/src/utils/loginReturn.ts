import type { UserRole, UserType } from '@/types';

const RETURN_TARGET_MAX_LENGTH = 2048;
const NON_ROUTE_PREFIXES = ['/api', '/uploads'] as const;
const AUTH_FLOW_PREFIXES = [
  '/login',
  '/signup',
  '/email-verify',
  '/password-reset',
  '/social-login',
  '/complete-profile',
] as const;
const ADMIN_PREFIX = '/admin';
const USER_PAYMENT_PREFIX = '/subscriptions/checkout';
const BUSINESS_PREFIX = '/company-certification';

interface LoginReturnLocation {
  pathname: string;
  search: string;
  hash?: string;
}

interface LoginReturnIdentity {
  role: UserRole;
  userType: UserType;
}

function matchesPathPrefix(pathname: string, prefix: string): boolean {
  return pathname === prefix || pathname.startsWith(`${prefix}/`);
}

function getCanonicalPathname(parsed: URL): string {
  return decodeURIComponent(parsed.pathname).toLowerCase();
}

export function getSafeLoginReturnTarget(candidate: string | null): string | null {
  if (
    !candidate ||
    candidate.length > RETURN_TARGET_MAX_LENGTH ||
    !candidate.startsWith('/') ||
    candidate.startsWith('//') ||
    candidate.includes('\\') ||
    candidate.includes('#')
  ) {
    return null;
  }

  let decodedCandidate: string;
  try {
    decodedCandidate = decodeURIComponent(candidate);
  } catch {
    return null;
  }

  if (
    decodedCandidate.startsWith('//') ||
    decodedCandidate.includes('\\') ||
    Array.from(decodedCandidate).some((character) => {
      const code = character.charCodeAt(0);
      return code <= 31 || code === 127;
    })
  ) {
    return null;
  }

  try {
    const base = new URL('https://local.invalid');
    const parsed = new URL(candidate, base);
    const normalizedTarget = `${parsed.pathname}${parsed.search}`;
    const canonicalPathname = getCanonicalPathname(parsed);

    if (parsed.origin !== base.origin || normalizedTarget !== candidate) return null;

    const isNonRoute = NON_ROUTE_PREFIXES.some((prefix) =>
      matchesPathPrefix(canonicalPathname, prefix),
    );
    const isAuthFlow = AUTH_FLOW_PREFIXES.some((prefix) =>
      matchesPathPrefix(canonicalPathname, prefix),
    );
    return isNonRoute || isAuthFlow ? null : normalizedTarget;
  } catch {
    return null;
  }
}

export function getAccessibleLoginReturnTarget(
  candidate: string | null,
  identity: LoginReturnIdentity,
): string | null {
  const safeTarget = getSafeLoginReturnTarget(candidate);
  if (!safeTarget) return null;

  const canonicalPathname = getCanonicalPathname(new URL(safeTarget, 'https://local.invalid'));
  if (matchesPathPrefix(canonicalPathname, ADMIN_PREFIX)) {
    return identity.role === 'ADMIN' ? safeTarget : null;
  }
  if (matchesPathPrefix(canonicalPathname, USER_PAYMENT_PREFIX)) {
    return identity.role === 'USER' ? safeTarget : null;
  }
  if (matchesPathPrefix(canonicalPathname, BUSINESS_PREFIX)) {
    return identity.role === 'USER' && identity.userType === 'BUSINESS' ? safeTarget : null;
  }
  return safeTarget;
}

export function createLoginPath(location: LoginReturnLocation): string {
  const returnTarget = getSafeLoginReturnTarget(`${location.pathname}${location.search}`);
  if (!returnTarget) return '/login';

  return `/login?${new URLSearchParams({ returnTo: returnTarget }).toString()}`;
}
