/** Format ISO date string to YYYY.MM.DD */
export function formatDate(iso: string | null | undefined): string {
  if (!iso) return '-';
  const d = new Date(iso);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}.${mm}.${dd}`;
}

/** Format ISO datetime string to YYYY.MM.DD HH:mm */
export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return '-';
  const d = new Date(iso);
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  const hh = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${yyyy}.${mm}.${dd} ${hh}:${min}`;
}

/** Format number with comma separators (Korean locale) */
export function formatNumber(n: number): string {
  return n.toLocaleString('ko-KR');
}

/** Format price with won symbol */
export function formatPrice(n: number): string {
  return `\u20A9${n.toLocaleString('ko-KR')}`;
}
