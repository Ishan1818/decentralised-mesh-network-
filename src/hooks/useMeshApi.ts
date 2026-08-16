import { useCallback, useEffect, useState } from "react";
import { api, connectWebSocket, type MeshSnapshot, type NetworkStatus } from "../lib/api";

export function useMeshApi() {
  const [snapshot, setSnapshot] = useState<MeshSnapshot | null>(null);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      const data = await api.getSnapshot();
      setSnapshot(data);
      setConnected(true);
      setError(null);
    } catch {
      setConnected(false);
      setError("Gateway offline — using local demo data");
    }
  }, []);

  useEffect(() => {
    refresh();
    const interval = setInterval(refresh, 5000);
    const disconnect = connectWebSocket((data) => {
      setSnapshot(data);
      setConnected(true);
      setError(null);
    });
    return () => {
      clearInterval(interval);
      disconnect();
    };
  }, [refresh]);

  const disableNode = useCallback(
    async (nodeId: string, mode: "centerpiece" | "network" = "centerpiece") => {
      try {
        await api.disableNode(nodeId, mode);
        await refresh();
      } catch {
        setError("Simulation action failed");
      }
    },
    [refresh]
  );

  const sendSos = useCallback(async () => {
    try {
      await api.sendSos();
      await refresh();
    } catch {
      setError("SOS simulation failed");
    }
  }, [refresh]);

  const reset = useCallback(async () => {
    try {
      await api.reset();
      await refresh();
    } catch {
      setError("Reset failed");
    }
  }, [refresh]);

  return {
    snapshot,
    status: snapshot?.status ?? null,
    connected,
    error,
    disableNode,
    sendSos,
    reset,
    refresh,
  };
}

export function useNetworkStatus(): NetworkStatus | null {
  const { status } = useMeshApi();
  return status;
}
