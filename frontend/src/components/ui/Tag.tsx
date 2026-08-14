import styles from './Tag.module.css';

interface TagProps {
  label: string;
  active?: boolean;
  onClick?: () => void;
  className?: string;
}

export default function Tag({ label, active = false, onClick, className }: TagProps) {
  const classes = [styles.tag, active ? styles.on : '', className ?? ''].filter(Boolean).join(' ');

  return (
    <button className={classes} onClick={onClick} type="button" aria-pressed={active}>
      {label}
    </button>
  );
}
