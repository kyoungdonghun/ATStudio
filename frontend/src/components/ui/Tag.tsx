import styles from './Tag.module.css';

interface TagProps {
  label: string;
  active?: boolean;
  onClick?: () => void;
  className?: string;
}

export default function Tag({
  label,
  active = false,
  onClick,
  className,
}: TagProps) {
  const classes = [styles.tag, active ? styles.on : '', className ?? '']
    .filter(Boolean)
    .join(' ');

  return (
    <span className={classes} onClick={onClick} role="button" tabIndex={0}>
      {label}
    </span>
  );
}
