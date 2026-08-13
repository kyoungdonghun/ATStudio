export function getSafeYoutubeUrl(value: string): string | null {
  if (!/^https:\/\//i.test(value) || value.includes('\\')) {
    return null;
  }

  try {
    const url = new URL(value);
    const hostname = url.hostname.toLowerCase();
    const allowedHost = hostname === 'youtube.com' || hostname.endsWith('.youtube.com');
    const allowedPort = url.port === '' || url.port === '443';

    if (
      url.protocol !== 'https:' ||
      url.username !== '' ||
      url.password !== '' ||
      !allowedHost ||
      !allowedPort
    ) {
      return null;
    }

    return url.href;
  } catch {
    return null;
  }
}
