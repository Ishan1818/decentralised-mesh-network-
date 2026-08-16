export type NetworkFilter =
  | "all"
  | "active"
  | "sos"
  | "weak"
  | "queue"
  | "responders"
  | "gateways";

export interface NetworkCardData {
  id: string;
  title: string;
  metric: string;
  submetric: string;
  description: string;
  filter: NetworkFilter[];
  connectivity?: string;
  nodes: { x: number; y: number }[];
  edges: [number, number][];
}

export const networkCards: NetworkCardData[] = [
  {
    id: "live-mesh",
    title: "Live Mesh",
    metric: "14 active nodes",
    submetric: "Connectivity: 92%",
    description: "Participating devices forming local communication paths.",
    filter: ["all", "active"],
    connectivity: "92%",
    nodes: [
      { x: 50, y: 30 },
      { x: 30, y: 70 },
      { x: 70, y: 70 },
      { x: 50, y: 90 },
    ],
    edges: [
      [0, 1],
      [0, 2],
      [1, 3],
      [2, 3],
    ],
  },
  {
    id: "sos-network",
    title: "SOS Network",
    metric: "3 active alerts",
    submetric: "Priority: Critical",
    description: "Emergency messages awaiting or in transit through the mesh.",
    filter: ["all", "sos"],
    nodes: [
      { x: 40, y: 50 },
      { x: 60, y: 30 },
      { x: 70, y: 70 },
    ],
    edges: [
      [0, 1],
      [0, 2],
    ],
  },
  {
    id: "route-discovery",
    title: "Route Discovery",
    metric: "7 available routes",
    submetric: "Avg hops: 3.2",
    description: "Alternate paths computed when direct links are unavailable.",
    filter: ["all", "active"],
    nodes: [
      { x: 20, y: 50 },
      { x: 50, y: 30 },
      { x: 50, y: 70 },
      { x: 80, y: 50 },
    ],
    edges: [
      [0, 1],
      [0, 2],
      [1, 3],
      [2, 3],
    ],
  },
  {
    id: "offline-queue",
    title: "Offline Queue",
    metric: "12 messages waiting",
    submetric: "Store-and-forward",
    description: "Messages held locally until a relay path becomes available.",
    filter: ["all", "queue"],
    nodes: [{ x: 50, y: 50 }],
    edges: [],
  },
  {
    id: "field-team",
    title: "Field Team",
    metric: "8 connected responders",
    submetric: "Sector coverage: 4",
    description: "Rescue personnel coordinating through the mesh network.",
    filter: ["all", "responders", "active"],
    nodes: [
      { x: 30, y: 40 },
      { x: 70, y: 40 },
      { x: 50, y: 80 },
    ],
    edges: [
      [0, 2],
      [1, 2],
    ],
  },
  {
    id: "gateway",
    title: "Gateway",
    metric: "1 connected gateway",
    submetric: "Uplink: intermittent",
    description: "Bridge node when external connectivity briefly returns.",
    filter: ["all", "gateways"],
    nodes: [
      { x: 50, y: 30 },
      { x: 30, y: 70 },
      { x: 70, y: 70 },
    ],
    edges: [
      [0, 1],
      [0, 2],
    ],
  },
];

export const filterLabels: { id: NetworkFilter; label: string }[] = [
  { id: "all", label: "All" },
  { id: "active", label: "Active" },
  { id: "sos", label: "SOS" },
  { id: "weak", label: "Weak Signal" },
  { id: "queue", label: "Offline Queue" },
  { id: "responders", label: "Responders" },
  { id: "gateways", label: "Gateways" },
];

export const meshHeroEdges: [string, string][] = [
  ["n1", "n2"],
  ["n1", "n3"],
  ["n2", "n4"],
  ["n3", "n4"],
  ["n4", "n5"],
  ["n5", "n6"],
  ["n3", "n6"],
];

export const meshHeroNodes = [
  { id: "n1", label: "01", x: 300, y: 60, battery: 82, neighbors: 2 },
  { id: "n2", label: "04", x: 180, y: 140, battery: 91, neighbors: 3 },
  { id: "n3", label: "07", x: 420, y: 140, battery: 76, neighbors: 3 },
  { id: "n4", label: "09", x: 300, y: 220, battery: 68, neighbors: 4 },
  { id: "n5", label: "11", x: 300, y: 320, battery: 54, neighbors: 2 },
  { id: "n6", label: "15", x: 480, y: 260, battery: 88, neighbors: 2 },
];

export const demoNetworkStats = {
  label: "DEMO NETWORK",
  nodes: 24,
  routes: 7,
  failures: 2,
  delivery: "94%",
};
