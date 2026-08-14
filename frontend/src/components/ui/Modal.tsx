import { type ReactNode, type RefObject, useEffect, useRef, useCallback, useId } from 'react';
import { createPortal } from 'react-dom';
import styles from './Modal.module.css';

interface OpenModalEntry {
  id: symbol;
  getElement: () => HTMLDivElement | null;
}

const openModalStack: OpenModalEntry[] = [];

function isAvailableFocusTarget(target: HTMLElement | null): target is HTMLElement {
  return Boolean(
    target?.isConnected &&
    !target.matches(':disabled, [aria-disabled="true"]') &&
    !target.closest('[hidden], [aria-hidden="true"], [inert]'),
  );
}

function focusIfAvailable(target: HTMLElement | null): boolean {
  if (!isAvailableFocusTarget(target)) return false;

  const needsTemporaryTabIndex = target.tabIndex < 0 && !target.hasAttribute('tabindex');
  if (needsTemporaryTabIndex) target.setAttribute('tabindex', '-1');
  target.focus();
  if (needsTemporaryTabIndex) target.removeAttribute('tabindex');

  return document.activeElement === target;
}

function focusMainFallback(): void {
  const mainRegions = Array.from(document.querySelectorAll<HTMLElement>('main, [role="main"]'));

  for (const mainRegion of mainRegions) {
    const heading = mainRegion.querySelector<HTMLElement>('h1, [role="heading"][aria-level="1"]');
    if (focusIfAvailable(heading)) return;
  }

  for (const mainRegion of mainRegions) {
    if (focusIfAvailable(mainRegion)) return;
  }
}

function focusHighestRemainingModal(excludedId: symbol): boolean {
  for (let index = openModalStack.length - 1; index >= 0; index -= 1) {
    const entry = openModalStack[index];
    if (entry.id !== excludedId && focusIfAvailable(entry.getElement())) return true;
  }

  return false;
}

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
  busy?: boolean;
  focusFallbackRef?: RefObject<HTMLElement>;
}

export default function Modal({
  open,
  onClose,
  title,
  children,
  busy = false,
  focusFallbackRef,
}: ModalProps) {
  const backdropRef = useRef<HTMLDivElement>(null);
  const modalRef = useRef<HTMLDivElement>(null);
  const returnFocusRef = useRef<HTMLElement | null>(null);
  const focusFallbackPropRef = useRef(focusFallbackRef);
  const modalIdRef = useRef(Symbol('modal'));
  const onCloseRef = useRef(onClose);
  const busyRef = useRef(busy);
  const titleId = useId();
  busyRef.current = busy;
  focusFallbackPropRef.current = focusFallbackRef;

  useEffect(() => {
    onCloseRef.current = onClose;
  }, [onClose]);

  const requestClose = useCallback(() => {
    if (!busyRef.current) onCloseRef.current();
  }, []);

  const handleKeyDown = useCallback(
    (e: KeyboardEvent) => {
      const topModal = openModalStack[openModalStack.length - 1];
      if (topModal?.id !== modalIdRef.current) return;

      if (e.key === 'Escape') {
        e.preventDefault();
        requestClose();
      }

      /* Focus trap */
      if (e.key === 'Tab' && modalRef.current) {
        const focusable = Array.from(
          modalRef.current.querySelectorAll<HTMLElement>(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])',
          ),
        ).filter(
          (element) =>
            !element.hasAttribute('disabled') && element.getAttribute('aria-hidden') !== 'true',
        );
        if (focusable.length === 0) {
          if (busyRef.current) {
            e.preventDefault();
            modalRef.current.focus();
          }
          return;
        }

        const first = focusable[0];
        const last = focusable[focusable.length - 1];
        const activeElement = document.activeElement;

        if (!modalRef.current.contains(activeElement)) {
          e.preventDefault();
          (e.shiftKey ? last : first).focus();
        } else if (e.shiftKey && (activeElement === first || activeElement === modalRef.current)) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && (activeElement === last || activeElement === modalRef.current)) {
          e.preventDefault();
          first.focus();
        }
      }
    },
    [requestClose],
  );

  useEffect(() => {
    if (!open) return;
    const modalId = modalIdRef.current;
    openModalStack.push({ id: modalId, getElement: () => modalRef.current });
    document.addEventListener('keydown', handleKeyDown);
    document.body.style.overflow = 'hidden';

    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      const stackIndex = openModalStack.map((entry) => entry.id).lastIndexOf(modalId);
      if (stackIndex >= 0) {
        openModalStack.splice(stackIndex, 1);
      }
      document.body.style.overflow = openModalStack.length > 0 ? 'hidden' : '';
    };
  }, [open, handleKeyDown]);

  useEffect(() => {
    if (!open) return;

    const modalId = modalIdRef.current;
    const opener = document.activeElement;
    returnFocusRef.current =
      opener instanceof HTMLElement && opener !== document.body ? opener : null;

    return () => {
      const returnTarget = returnFocusRef.current;
      returnFocusRef.current = null;
      if (focusIfAvailable(returnTarget)) return;
      if (focusIfAvailable(focusFallbackPropRef.current?.current ?? null)) return;
      if (focusHighestRemainingModal(modalId)) return;
      focusMainFallback();
    };
  }, [open]);

  /* Auto-focus modal on open */
  useEffect(() => {
    if (open && modalRef.current) {
      modalRef.current.focus();
    }
  }, [open]);

  if (!open) return null;

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === backdropRef.current) {
      requestClose();
    }
  };

  return createPortal(
    <div className={styles.backdrop} ref={backdropRef} onClick={handleBackdropClick}>
      <div
        className={styles.modal}
        ref={modalRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? titleId : undefined}
        aria-busy={busy || undefined}
        tabIndex={-1}
      >
        {title && (
          <div className={styles.header}>
            <h2 className={styles.title} id={titleId}>
              {title}
            </h2>
            <button
              type="button"
              className={styles.closeBtn}
              onClick={requestClose}
              aria-label="닫기"
              title="닫기"
              disabled={busy}
            >
              X
            </button>
          </div>
        )}
        {children}
      </div>
    </div>,
    document.body,
  );
}
