import { useId } from 'react';
import Button from '@/components/ui/Button';
import Modal from '@/components/ui/Modal';
import styles from './ConfirmDialog.module.css';

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmVariant?: 'primary' | 'danger';
  busy?: boolean;
  typedConfirmation?: {
    label: string;
    requiredText: string;
    value: string;
    hint?: string;
    onChange: (value: string) => void;
  };
  onConfirm: () => void;
  onCancel: () => void;
}

export default function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = '확인',
  cancelLabel = '취소',
  confirmVariant = 'primary',
  busy = false,
  typedConfirmation,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  const typedInputID = useId();
  const typedConfirmationMatches =
    !typedConfirmation || typedConfirmation.value.trim() === typedConfirmation.requiredText;

  const handleCancel = () => {
    if (!busy) onCancel();
  };

  const handleConfirm = () => {
    if (!busy && typedConfirmationMatches) onConfirm();
  };

  return (
    <Modal open={open} onClose={handleCancel} title={title} busy={busy}>
      <div className={styles.body}>
        <p className={styles.message}>{message}</p>
        {typedConfirmation ? (
          <div className={styles.typedField}>
            <label className={styles.typedLabel} htmlFor={typedInputID}>
              {typedConfirmation.label}
            </label>
            <input
              id={typedInputID}
              className={styles.typedInput}
              type="text"
              value={typedConfirmation.value}
              disabled={busy}
              autoComplete="off"
              spellCheck={false}
              onChange={(event) => typedConfirmation.onChange(event.target.value)}
            />
            {typedConfirmation.hint ? (
              <span className={styles.typedHint}>{typedConfirmation.hint}</span>
            ) : null}
          </div>
        ) : null}
        <div className={styles.actions}>
          <Button variant="ghost" onClick={handleCancel} disabled={busy}>
            {cancelLabel}
          </Button>
          <Button
            variant={confirmVariant}
            onClick={handleConfirm}
            loading={busy}
            disabled={!typedConfirmationMatches}
          >
            {confirmLabel}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
