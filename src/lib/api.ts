const API_BASE = import.meta.env.PUBLIC_API_URL ?? "";

export interface NetworkStatus {
  meshActive: boolean;
  nodesNearby: number;
  routes: number;
  sosCount: number;
  internet: boolean;
  cellular: boolean;
  communicationAvailable: boolean;
}

export interface ApiNode {
  id: string;
  label: string;
  role: string;
  battery: number;
  neighbors: number;
  signal: string;
  sector: string;
  status: string;
  position: { x: number; y: number };
}

export interface CenterpieceState {
  nodes: { id: string; label: string; disabled: boolean }[];
  edges: [string, string][];
  activeRoute: string[];
  status: string;
}

export interface MeshSnapshot {
  status: NetworkStatus;
  nodes: ApiNode[];
  edges: [string, string][];
  centerpiece: CenterpieceState;
  messages: unknown[];
}

async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
  });
  if (!res.ok) throw new Error(`API ${path} failed: ${res.status}`);
  return res.json() as Promise<T>;
}

export const api = {
  health: () => apiFetch<{ ok: boolean }>("/api/health"),
  getStatus: () => apiFetch<NetworkStatus>("/api/network/status"),
  getSnapshot: () => apiFetch<MeshSnapshot>("/api/snapshot"),
  getCenterpiece: () => apiFetch<CenterpieceState>("/api/centerpiece"),
  getRoute: (targetId: string) =>
    apiFetch<{ route: string[]; hops: number; reliability: number; battery: string; stability: string }>(
      `/api/routes/${targetId}`
    ),
  disableNode: (nodeId: string, mode: "centerpiece" | "network" = "centerpiece") =>
    apiFetch<{ success: boolean; route?: string[]; status?: string }>("/api/simulate/disable", {
      method: "POST",
      body: JSON.stringify({ nodeId, mode }),
    }),
  sendSos: (from?: string, to?: string) =>
    apiFetch<{ success: boolean; route?: string[]; status?: string }>("/api/simulate/sos", {
      method: "POST",
      body: JSON.stringify({ from, to }),
    }),
  reset: () =>
    apiFetch<{ success: boolean }>("/api/simulate/reset", { method: "POST" }),
};

export function connectWebSocket(onMessage: (data: MeshSnapshot) => void): () => void {
  const wsUrl = API_BASE
    ? API_BASE.replace(/^http/, "ws") + "/ws"
    : `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.hostname}:4000/ws`;

  let ws: WebSocket | null = null;
  let closed = false;

  try {
    ws = new WebSocket(wsUrl);
    ws.onmessage = (e) => {
      const msg = JSON.parse(e.data);
      if (msg.type === "snapshot" || msg.type === "update") onMessage(msg.data);
    };
    ws.onerror = () => {};
  } catch {
    // WebSocket unavailable — polling fallback handled by hook
  }

  return () => {
    closed = true;
    ws?.close();
  };
}
