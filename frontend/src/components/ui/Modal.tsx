import { type ReactNode, useEffect, useRef, useCallback, useId } from 'react';
import { createPortal } from 'react-dom';
import styles from './Modal.module.css';

const openModalStack: symbol[] = [];

interface ModalProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  children: ReactNode;
}

export default function Modal({ open, onClose, title, children }: ModalProps) {
  const backdropRef = useRef<HTMLDivElement>(null);
  const modalRef = useRef<HTMLDivElement>(null);
  const returnFocusRef = useRef<HTMLElement | null>(null);
  const modalIdRef = useRef(Symbol('modal'));
  const onCloseRef = useRef(onClose);
  const titleId = useId();

  useEffect(() => {
    onCloseRef.current = onClose;
  }, [onClose]);

  const handleKeyDown = useCallback((e: KeyboardEvent) => {
    const topModalId = openModalStack[openModalStack.length - 1];
    if (topModalId !== modalIdRef.current) return;

    if (e.key === 'Escape') {
      onCloseRef.current();
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
      if (focusable.length === 0) return;

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
  }, []);

  useEffect(() => {
    if (!open) return;
    const modalId = modalIdRef.current;
    openModalStack.push(modalId);
    document.addEventListener('keydown', handleKeyDown);
    document.body.style.overflow = 'hidden';

    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      const stackIndex = openModalStack.lastIndexOf(modalId);
      if (stackIndex >= 0) {
        openModalStack.splice(stackIndex, 1);
      }
      document.body.style.overflow = openModalStack.length > 0 ? 'hidden' : '';
    };
  }, [open, handleKeyDown]);

  useEffect(() => {
    if (!open) return;

    const opener = document.activeElement;
    returnFocusRef.current =
      opener instanceof HTMLElement && opener !== document.body ? opener : null;

    return () => {
      const returnTarget = returnFocusRef.current;
      returnFocusRef.current = null;
      if (
        returnTarget?.isConnected &&
        returnTarget.getAttribute('aria-disabled') !== 'true' &&
        !('disabled' in returnTarget && returnTarget.disabled)
      ) {
        returnTarget.focus();
      }
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
      onClose();
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
              onClick={onClose}
              aria-label="닫기"
              title="닫기"
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
