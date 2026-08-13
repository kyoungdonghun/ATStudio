import { useCallback, useEffect, useRef, type RefObject } from 'react';
import { useBlocker } from 'react-router-dom';

export default function usePendingMutationGuard(
  operationRef: RefObject<unknown | null>,
  pending: boolean,
) {
  const blocker = useBlocker(useCallback(() => operationRef.current !== null, [operationRef]));
  const blockerRef = useRef(blocker);
  blockerRef.current = blocker;

  useEffect(() => {
    if (!pending) return;

    const handleBeforeUnload = (event: BeforeUnloadEvent) => {
      if (operationRef.current === null) return;
      event.preventDefault();
      event.returnValue = '';
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [operationRef, pending]);

  return useCallback(() => {
    const currentBlocker = blockerRef.current;
    if (currentBlocker.state === 'blocked') currentBlocker.reset();
  }, []);
}
