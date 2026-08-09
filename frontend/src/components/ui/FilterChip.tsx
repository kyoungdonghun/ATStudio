import styles from './FilterChip.module.css';

interface FilterChipProps {
  label: string;
  active?: boolean;
  onClick?: () => void;
  className?: string;
}

export default function FilterChip({ label, active = false, onClick, className }: FilterChipProps) {
  const classes = [styles.chip, active ? styles.on : '', className ?? ''].filter(Boolean).join(' ');

  return (
    <button className={classes} onClick={onClick} type="button" aria-pressed={active}>
      {label}
    </button>
  );
}
