import { createContext, useContext, type ReactNode } from "react";
import { useMeshApi } from "../hooks/useMeshApi";
import type { MeshSnapshot } from "../lib/api";

interface MeshContextValue {
  snapshot: MeshSnapshot | null;
  connected: boolean;
  error: string | null;
  disableNode: (nodeId: string, mode?: "centerpiece" | "network") => Promise<void>;
  sendSos: () => Promise<void>;
  reset: () => Promise<void>;
  refresh: () => Promise<void>;
}

const MeshContext = createContext<MeshContextValue | null>(null);

export function MeshProvider({ children }: { children: ReactNode }) {
  const value = useMeshApi();
  return <MeshContext.Provider value={value}>{children}</MeshContext.Provider>;
}

export function useMesh() {
  const ctx = useContext(MeshContext);
  if (!ctx) throw new Error("useMesh must be used within MeshProvider");
  return ctx;
}

export function useMeshOptional() {
  return useContext(MeshContext);
}
