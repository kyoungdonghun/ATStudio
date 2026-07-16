import type { ReactNode } from 'react';
import styles from './Badge.module.css';

type BadgeVariant = 'new' | 'hot' | 'accent';

interface BadgeProps {
  variant?: BadgeVariant;
  children: ReactNode;
  className?: string;
}

export default function Badge({ variant = 'accent', children, className }: BadgeProps) {
  const classes = [styles.badge, styles[variant], className ?? ''].filter(Boolean).join(' ');

  return <span className={classes}>{children}</span>;
}
