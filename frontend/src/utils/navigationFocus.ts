const MAIN_SELECTOR = 'main, [role="main"]';
const MAIN_HEADING_SELECTOR = 'h1, [role="heading"][aria-level="1"]';
const INACTIVE_ANCESTOR_SELECTOR = '[hidden], [aria-hidden="true"], [inert]';

let navigationDestinationFocusPending = false;

function isAvailableFocusTarget(element: HTMLElement): boolean {
  return element.isConnected && !element.closest(INACTIVE_ANCESTOR_SELECTOR);
}

function focusWithoutTabIndexTrace(element: HTMLElement): boolean {
  const hadTabIndex = element.hasAttribute('tabindex');

  if (!hadTabIndex) element.setAttribute('tabindex', '-1');
  try {
    element.focus();
    return element.ownerDocument.activeElement === element;
  } finally {
    if (!hadTabIndex) element.removeAttribute('tabindex');
  }
}

export function focusNavigationDestination(
  targetDocument: Document | undefined = typeof document === 'undefined' ? undefined : document,
): boolean {
  if (!targetDocument) return false;

  const mainRegions = Array.from(
    targetDocument.querySelectorAll<HTMLElement>(MAIN_SELECTOR),
  ).filter(isAvailableFocusTarget);

  for (const mainRegion of mainRegions) {
    const headings = Array.from(mainRegion.querySelectorAll<HTMLElement>(MAIN_HEADING_SELECTOR));
    const heading = headings.find(isAvailableFocusTarget);
    if (heading && focusWithoutTabIndexTrace(heading)) return true;
  }

  const mainRegion = mainRegions[0];
  return mainRegion ? focusWithoutTabIndexTrace(mainRegion) : false;
}

export function requestNavigationDestinationFocus(): void {
  navigationDestinationFocusPending = true;
}

export function consumeNavigationDestinationFocus(
  targetDocument: Document | undefined = typeof document === 'undefined' ? undefined : document,
): void {
  if (!navigationDestinationFocusPending) return;

  navigationDestinationFocusPending = false;
  focusNavigationDestination(targetDocument);
}
