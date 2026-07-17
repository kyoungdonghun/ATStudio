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
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  const handleCancel = () => {
    if (!busy) onCancel();
  };

  const handleConfirm = () => {
    if (!busy) onConfirm();
  };

  return (
    <Modal open={open} onClose={handleCancel} title={title}>
      <div className={styles.body}>
        <p className={styles.message}>{message}</p>
        <div className={styles.actions}>
          <Button variant="ghost" onClick={handleCancel} disabled={busy}>
            {cancelLabel}
          </Button>
          <Button variant={confirmVariant} onClick={handleConfirm} loading={busy}>
            {confirmLabel}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
