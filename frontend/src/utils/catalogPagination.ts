export const PUBLIC_CATALOG_PAGE_SIZE = 20;

const POSITIVE_INTEGER_PATTERN = /^[1-9]\d*$/;

export function normalizeCatalogPage(rawPage: string | null): number {
  if (!rawPage || !POSITIVE_INTEGER_PATTERN.test(rawPage)) return 1;

  const page = Number(rawPage);
  return Number.isSafeInteger(page) ? page : 1;
}

export function getCatalogTotalPages(total: number, pageSize: number): number {
  if (!Number.isFinite(total) || total <= 0) return 1;
  if (!Number.isFinite(pageSize) || pageSize <= 0) return 1;
  return Math.max(1, Math.ceil(total / pageSize));
}

export function withCatalogQuery(pathname: string, searchParams: URLSearchParams): string {
  const query = searchParams.toString();
  return query ? `${pathname}?${query}` : pathname;
}
