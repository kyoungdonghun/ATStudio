const POSITIVE_DECIMAL_ID_PATTERN = /^[1-9][0-9]*$/;

export function parsePositiveDecimalRouteID(rawID: string | undefined): number | null {
  if (
    rawID === undefined ||
    rawID.length === 0 ||
    rawID.trim() !== rawID ||
    !POSITIVE_DECIMAL_ID_PATTERN.test(rawID)
  ) {
    return null;
  }

  const parsedID = Number(rawID);
  return Number.isSafeInteger(parsedID) ? parsedID : null;
}
