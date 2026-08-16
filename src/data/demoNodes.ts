export type NodeStatus = "online" | "offline" | "weak" | "relay";
export type NodeRole = "relay" | "responder" | "gateway" | "civilian" | "sos";

export interface MeshNode {
  id: string;
  label: string;
  role: NodeRole;
  battery: number;
  neighbors: number;
  signal: "strong" | "moderate" | "weak";
  sector: string;
  status: NodeStatus;
  position: { x: number; y: number };
  category: "active" | "sos" | "weak" | "queue" | "responder" | "gateway";
}

export const demoNodes: MeshNode[] = [
  { id: "NODE-04", label: "NODE-04", role: "relay", battery: 82, neighbors: 3, signal: "strong", sector: "Sector 01", status: "online", position: { x: 120, y: 80 }, category: "active" },
  { id: "NODE-07", label: "NODE-07", role: "civilian", battery: 91, neighbors: 4, signal: "strong", sector: "Sector 02", status: "online", position: { x: 200, y: 140 }, category: "active" },
  { id: "NODE-09", label: "NODE-09", role: "relay", battery: 72, neighbors: 5, signal: "moderate", sector: "Sector 02", status: "online", position: { x: 280, y: 100 }, category: "active" },
  { id: "NODE-11", label: "NODE-11", role: "relay", battery: 68, neighbors: 3, signal: "moderate", sector: "Sector 03", status: "online", position: { x: 360, y: 160 }, category: "active" },
  { id: "NODE-15", label: "NODE-15", role: "relay", battery: 76, neighbors: 4, signal: "strong", sector: "Sector 03", status: "online", position: { x: 440, y: 120 }, category: "active" },
  { id: "NODE-18", label: "NODE-18", role: "responder", battery: 76, neighbors: 5, signal: "strong", sector: "Sector 03", status: "online", position: { x: 520, y: 180 }, category: "responder" },
  { id: "NODE-12", label: "NODE-12", role: "relay", battery: 45, neighbors: 2, signal: "weak", sector: "Sector 12", status: "weak", position: { x: 160, y: 220 }, category: "weak" },
  { id: "NODE-21", label: "NODE-21", role: "gateway", battery: 88, neighbors: 6, signal: "strong", sector: "Gateway", status: "online", position: { x: 600, y: 100 }, category: "gateway" },
  { id: "NODE-03", label: "NODE-03", role: "civilian", battery: 54, neighbors: 2, signal: "moderate", sector: "Sector 03", status: "online", position: { x: 80, y: 160 }, category: "active" },
  { id: "SOS-01", label: "SOS-01", role: "sos", battery: 34, neighbors: 1, signal: "weak", sector: "Sector 03", status: "weak", position: { x: 300, y: 240 }, category: "sos" },
];

export const nodeDetail = {
  id: "NODE-18",
  title: "RESCUE TEAM · SECTOR 03",
  status: "ONLINE" as const,
  signal: "Strong",
  battery: 76,
  neighbors: 5,
  role: "Relay",
  routes: 7,
  queue: 2,
  lastSeen: "2 sec ago",
  story:
    "NODE-18 entered the network 4 minutes ago and is currently acting as a high-reliability relay between two disconnected sections.",
};

export const centerpieceNodes = [
  { id: "A", label: "A", x: 200, y: 200 },
  { id: "B", label: "B", x: 300, y: 100 },
  { id: "C", label: "C", x: 400, y: 100 },
  { id: "D", label: "D", x: 300, y: 220 },
  { id: "E", label: "E", x: 450, y: 200 },
  { id: "F", label: "F", x: 550, y: 280 },
];

export const centerpieceEdges: [string, string][] = [
  ["A", "B"],
  ["B", "C"],
  ["A", "D"],
  ["C", "D"],
  ["C", "E"],
  ["D", "E"],
  ["E", "F"],
];
