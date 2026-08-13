import { useCallback, useEffect, useRef, useState } from 'react';
import { fetchPublicCapabilities, type PublicCapabilitiesResponse } from '@/api/auth';

export type PublicCapabilitiesStatus = 'loading' | 'ready' | 'error';

export function usePublicCapabilities() {
  const [capabilities, setCapabilities] = useState<PublicCapabilitiesResponse | null>(null);
  const [status, setStatus] = useState<PublicCapabilitiesStatus>('loading');
  const [error, setError] = useState('');
  const requestIdRef = useRef(0);

  const loadCapabilities = useCallback(async () => {
    const requestId = ++requestIdRef.current;
    setCapabilities(null);
    setError('');
    setStatus('loading');

    try {
      const result = await fetchPublicCapabilities();
      if (requestId !== requestIdRef.current) return;

      setCapabilities(result);
      setStatus('ready');
    } catch {
      if (requestId !== requestIdRef.current) return;

      setError('로그인 환경 설정을 불러오지 못했습니다.');
      setStatus('error');
    }
  }, []);

  useEffect(() => {
    void loadCapabilities();

    return () => {
      requestIdRef.current += 1;
    };
  }, [loadCapabilities]);

  return {
    capabilities,
    loading: status === 'loading',
    error,
    status,
    retry: loadCapabilities,
  };
}
