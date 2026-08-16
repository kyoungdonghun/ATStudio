import { useState } from 'react';

interface CatalogImageProps {
  src: string;
  alt: string;
  fallbackLabel: string;
  className?: string;
  fallbackClassName?: string;
}

export default function CatalogImage({
  src,
  alt,
  fallbackLabel,
  className,
  fallbackClassName,
}: CatalogImageProps) {
  const [failedSrc, setFailedSrc] = useState<string | null>(null);

  if (failedSrc === src) {
    return (
      <span className={fallbackClassName} role="img" aria-label={fallbackLabel}>
        {'\u266A'}
      </span>
    );
  }

  return <img src={src} alt={alt} className={className} onError={() => setFailedSrc(src)} />;
}
