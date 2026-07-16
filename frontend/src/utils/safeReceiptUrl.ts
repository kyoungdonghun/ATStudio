export function getSafeReceiptUrl(value: string | null | undefined): string | null {
  if (!value) return null;

  try {
    const url = new URL(value);
    const allowedPort = url.port === '' || url.port === '443';

    if (
      url.protocol !== 'https:' ||
      url.username !== '' ||
      url.password !== '' ||
      !url.hostname ||
      !allowedPort
    ) {
      return null;
    }

    return url.href;
  } catch {
    return null;
  }
}
