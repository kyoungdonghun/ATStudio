import { useToastStore } from '@/store/toastStore';
import type { ToastType } from '@/store/toastStore';
import styles from './ToastContainer.module.css';

const TYPE_CLASS: Record<ToastType, string> = {
  success: styles.success,
  error: styles.error,
  warning: styles.warning,
  info: styles.info,
};

export default function ToastContainer() {
  const toasts = useToastStore((s) => s.toasts);
  const dismiss = useToastStore((s) => s.dismiss);

  if (toasts.length === 0) return null;

  return (
    <div className={styles.container} aria-label="알림">
      {toasts.map((t) => (
        <div
          key={t.id}
          className={`${styles.toast} ${TYPE_CLASS[t.type]}`}
          role={t.type === 'error' ? 'alert' : 'status'}
          aria-live={t.type === 'error' ? 'assertive' : 'polite'}
          aria-atomic="true"
        >
          <span className={styles.message}>{t.message}</span>
          <button
            type="button"
            className={styles.dismissButton}
            onClick={() => dismiss(t.id)}
            aria-label={`${t.message} 알림 닫기`}
            title="알림 닫기"
          >
            {'\u00D7'}
          </button>
        </div>
      ))}
    </div>
  );
}
