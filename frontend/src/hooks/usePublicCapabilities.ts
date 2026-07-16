import { useEffect, useState } from 'react';
import { fetchPublicCapabilities, type PublicCapabilitiesResponse } from '@/api/auth';

export function usePublicCapabilities() {
  const [capabilities, setCapabilities] = useState<PublicCapabilitiesResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;

    void (async () => {
      try {
        const result = await fetchPublicCapabilities();
        if (!active) return;

        setCapabilities(result);
      } catch {
        if (!active) return;
        setError('로그인 환경 설정을 불러오지 못했습니다.');
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    })();

    return () => {
      active = false;
    };
  }, []);

  return { capabilities, loading, error };
}
