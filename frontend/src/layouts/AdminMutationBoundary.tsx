import { createContext, useContext } from 'react';

export interface AdminMutationBoundary {
  acquire(owner: object): void;
  release(owner: object): void;
  hasActiveOwner(): boolean;
}

const isolatedPageBoundary: AdminMutationBoundary = {
  acquire: () => undefined,
  release: () => undefined,
  hasActiveOwner: () => false,
};

export const AdminMutationBoundaryContext = createContext(isolatedPageBoundary);

export function useAdminMutationBoundary(): AdminMutationBoundary {
  return useContext(AdminMutationBoundaryContext);
}
