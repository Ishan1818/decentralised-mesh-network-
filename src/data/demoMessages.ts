export interface DemoMessage {
  id: string;
  title: string;
  sector: string;
  priority: "CRITICAL" | "HIGH" | "NORMAL";
  location: { lat: string; lng: string };
  route: string[];
  deliveredAt: string;
  status: "DELIVERED" | "IN_TRANSIT" | "STORED";
  body: string;
}

export const sosMessage: DemoMessage = {
  id: "SOS-2026-0142",
  title: "PERSON TRAPPED",
  sector: "SECTOR 03",
  priority: "CRITICAL",
  location: { lat: "26.9124° N", lng: "75.7873° E" },
  route: ["NODE-03", "NODE-07", "NODE-11", "NODE-18"],
  deliveredAt: "12 seconds ago",
  status: "DELIVERED",
  body: "3 injured civilians at Sector 12.",
};

export const storeForwardMessage = {
  body: "3 injured civilians at Sector 12.",
  status: "STORED LOCALLY" as const,
  steps: [
    "NODE-12 ENTERS RANGE",
    "MESSAGE TRANSFERRED",
    "NODE-21 RECONNECTS",
    "DELIVERED",
  ],
};

export const assistantRoutes: Record<string, { route: string[]; hops: number; reliability: number; battery: string; stability: string }> = {
  default: {
    route: ["NODE-04", "NODE-09", "NODE-15", "NODE-18"],
    hops: 4,
    reliability: 94,
    battery: "good",
    stability: "high",
  },
  sos: {
    route: ["NODE-03", "NODE-07", "NODE-11", "NODE-18"],
    hops: 4,
    reliability: 91,
    battery: "moderate",
    stability: "high",
  },
  disconnected: {
    route: ["Sector 12", "NODE-12", "—", "—"],
    hops: 0,
    reliability: 0,
    battery: "n/a",
    stability: "none",
  },
  relays: {
    route: ["NODE-09", "NODE-15", "NODE-18"],
    hops: 3,
    reliability: 96,
    battery: "good",
    stability: "high",
  },
  weakest: {
    route: ["Sector 12", "NODE-12"],
    hops: 1,
    reliability: 42,
    battery: "low",
    stability: "weak",
  },
  alternate: {
    route: ["NODE-04", "NODE-11", "NODE-15", "NODE-18"],
    hops: 4,
    reliability: 89,
    battery: "good",
    stability: "moderate",
  },
};
