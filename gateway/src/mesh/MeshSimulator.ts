import { EventEmitter } from "node:events";
import { buildGraph, findRoute, getActiveEdges } from "./routing.js";

export interface SimNode {
  id: string;
  label: string;
  role: "relay" | "responder" | "gateway" | "civilian" | "sos";
  battery: number;
  neighbors: number;
  signal: "strong" | "moderate" | "weak";
  sector: string;
  status: "online" | "offline" | "weak";
  position: { x: number; y: number };
}

export interface MeshMessage {
  id: string;
  title: string;
  sector: string;
  priority: "CRITICAL" | "HIGH" | "NORMAL";
  body: string;
  route: string[];
  status: "DELIVERED" | "IN_TRANSIT" | "STORED";
  deliveredAt?: string;
}

export interface NetworkStatus {
  meshActive: boolean;
  nodesNearby: number;
  routes: number;
  sosCount: number;
  internet: boolean;
  cellular: boolean;
  communicationAvailable: boolean;
}

const INITIAL_NODES: SimNode[] = [
  { id: "NODE-04", label: "04", role: "relay", battery: 82, neighbors: 3, signal: "strong", sector: "Sector 01", status: "online", position: { x: 120, y: 80 } },
  { id: "NODE-07", label: "07", role: "civilian", battery: 91, neighbors: 4, signal: "strong", sector: "Sector 02", status: "online", position: { x: 200, y: 140 } },
  { id: "NODE-09", label: "09", role: "relay", battery: 72, neighbors: 5, signal: "moderate", sector: "Sector 02", status: "online", position: { x: 280, y: 100 } },
  { id: "NODE-11", label: "11", role: "relay", battery: 68, neighbors: 3, signal: "moderate", sector: "Sector 03", status: "online", position: { x: 360, y: 160 } },
  { id: "NODE-15", label: "15", role: "relay", battery: 76, neighbors: 4, signal: "strong", sector: "Sector 03", status: "online", position: { x: 440, y: 120 } },
  { id: "NODE-18", label: "18", role: "responder", battery: 76, neighbors: 5, signal: "strong", sector: "Sector 03", status: "online", position: { x: 520, y: 180 } },
  { id: "NODE-12", label: "12", role: "relay", battery: 45, neighbors: 2, signal: "weak", sector: "Sector 12", status: "weak", position: { x: 160, y: 220 } },
  { id: "NODE-21", label: "21", role: "gateway", battery: 88, neighbors: 6, signal: "strong", sector: "Gateway", status: "online", position: { x: 600, y: 100 } },
  { id: "NODE-03", label: "03", role: "civilian", battery: 54, neighbors: 2, signal: "moderate", sector: "Sector 03", status: "online", position: { x: 80, y: 160 } },
  { id: "SOS-01", label: "SOS", role: "sos", battery: 34, neighbors: 1, signal: "weak", sector: "Sector 03", status: "weak", position: { x: 300, y: 240 } },
];

const INITIAL_EDGES: [string, string][] = [
  ["NODE-03", "NODE-04"],
  ["NODE-04", "NODE-07"],
  ["NODE-07", "NODE-09"],
  ["NODE-09", "NODE-11"],
  ["NODE-11", "NODE-15"],
  ["NODE-15", "NODE-18"],
  ["NODE-09", "NODE-21"],
  ["NODE-04", "NODE-12"],
  ["NODE-11", "SOS-01"],
  ["NODE-03", "NODE-07"],
];

const CENTERPIECE_NODES = ["A", "B", "C", "D", "E", "F"];
const CENTERPIECE_EDGES: [string, string][] = [
  ["A", "B"], ["B", "C"], ["A", "D"], ["C", "D"], ["C", "E"], ["D", "E"], ["E", "F"],
];

export class MeshSimulator extends EventEmitter {
  private nodes: SimNode[] = structuredClone(INITIAL_NODES);
  private edges: [string, string][] = [...INITIAL_EDGES];
  private excludedNodes = new Set<string>();
  private centerpieceExcluded = new Set<string>();
  private activeRoute: string[] = [];
  private centerpieceRoute: string[] = [];
  private status = "";
  private messages: MeshMessage[] = [
    {
      id: "SOS-2026-0142",
      title: "PERSON TRAPPED",
      sector: "SECTOR 03",
      priority: "CRITICAL",
      body: "3 injured civilians at Sector 12.",
      route: ["NODE-03", "NODE-07", "NODE-11", "NODE-18"],
      status: "DELIVERED",
      deliveredAt: "12 seconds ago",
    },
  ];
  private eventLog: { time: string; category: string; message: string }[] = [];

  constructor() {
    super();
    this.log("SYSTEM", "Mesh simulator initialized");
  }

  private log(category: string, message: string) {
    const entry = { time: new Date().toISOString(), category, message };
    this.eventLog.unshift(entry);
    if (this.eventLog.length > 100) this.eventLog.pop();
    this.emit("event", entry);
  }

  getStatus(): NetworkStatus {
    const online = this.nodes.filter((n) => n.status === "online" && !this.excludedNodes.has(n.id));
    return {
      meshActive: online.length > 0,
      nodesNearby: online.length,
      routes: this.countRoutes(),
      sosCount: this.messages.filter((m) => m.priority === "CRITICAL").length,
      internet: false,
      cellular: false,
      communicationAvailable: online.length >= 2,
    };
  }

  getNodes() {
    return this.nodes.map((n) => ({
      ...n,
      status: this.excludedNodes.has(n.id) ? "offline" as const : n.status,
    }));
  }

  getEdges() {
    return getActiveEdges(this.edges, this.excludedNodes);
  }

  getMessages() {
    return this.messages;
  }

  getEventLog() {
    return this.eventLog;
  }

  getCenterpiece() {
    return {
      nodes: CENTERPIECE_NODES.map((id) => ({ id, label: id, disabled: this.centerpieceExcluded.has(id) })),
      edges: getActiveEdges(CENTERPIECE_EDGES, this.centerpieceExcluded),
      activeRoute: this.centerpieceRoute,
      status: this.status,
    };
  }

  findRouteBetween(from: string, to: string): string[] | null {
    const graph = buildGraph(this.getEdges());
    return findRoute(graph, from, to, this.excludedNodes);
  }

  disableNode(nodeId: string) {
    this.excludedNodes.add(nodeId);
    const node = this.nodes.find((n) => n.id === nodeId);
    if (node) node.status = "offline";
    this.log("ROUTING", `Node ${nodeId} disabled`);
    this.emit("update", this.getSnapshot());
    return { success: true, nodeId };
  }

  disableCenterpieceNode(nodeId: string) {
    this.centerpieceExcluded.add(nodeId);
    this.status = "ROUTE FAILURE DETECTED";
    this.centerpieceRoute = [];
    this.log("ROUTING", `Centerpiece node ${nodeId} disabled`);

    const graph = buildGraph(getActiveEdges(CENTERPIECE_EDGES, this.centerpieceExcluded));
    const route = findRoute(graph, "A", "F", this.centerpieceExcluded);
    if (route) {
      this.centerpieceRoute = route;
      this.status = `ROUTE RECOVERED · +${route.length - 2} HOP`;
      this.log("ROUTING", `Recovered route: ${route.join(" → ")}`);
    } else {
      this.status = "NO ROUTE AVAILABLE";
    }

    this.emit("update", this.getSnapshot());
    return { success: true, route: this.centerpieceRoute, status: this.status };
  }

  sendSos(from = "A", to = "F") {
    const graph = buildGraph(getActiveEdges(CENTERPIECE_EDGES, this.centerpieceExcluded));
    const route = this.centerpieceRoute.length > 1
      ? this.centerpieceRoute
      : findRoute(graph, from, to, this.centerpieceExcluded);

    if (!route || route.length < 2) {
      this.status = "SOS FAILED — NO ROUTE";
      this.emit("update", this.getSnapshot());
      return { success: false, status: this.status };
    }

    this.centerpieceRoute = route;
    const msg: MeshMessage = {
      id: `SOS-${Date.now()}`,
      title: "SOS ALERT",
      sector: "DEMO",
      priority: "CRITICAL",
      body: "Emergency packet transmitted through mesh.",
      route: route.map((id) => `NODE-${id}`),
      status: "DELIVERED",
      deliveredAt: "just now",
    };
    this.messages.unshift(msg);
    this.status = "SOS RECEIVED";
    this.log("SOS", `SOS delivered via ${route.join(" → ")}`);
    this.emit("update", this.getSnapshot());
    return { success: true, route, status: this.status, message: msg };
  }

  reset() {
    this.nodes = structuredClone(INITIAL_NODES);
    this.edges = [...INITIAL_EDGES];
    this.excludedNodes.clear();
    this.centerpieceExcluded.clear();
    this.activeRoute = [];
    this.centerpieceRoute = [];
    this.status = "";
    this.log("SYSTEM", "Network reset");
    this.emit("update", this.getSnapshot());
    return { success: true };
  }

  getRouteToNode(targetId: string) {
    const graph = buildGraph(this.getEdges());
    const localId = "NODE-04";
    const route = findRoute(graph, localId, targetId, this.excludedNodes);
    if (!route) return null;

    const hops = route.length - 1;
    const avgBattery = route
      .map((id) => this.nodes.find((n) => n.id === id)?.battery ?? 50)
      .reduce((a, b) => a + b, 0) / route.length;

    return {
      route,
      hops,
      reliability: Math.min(99, Math.round(70 + avgBattery * 0.25 - hops * 3)),
      battery: avgBattery > 70 ? "good" : avgBattery > 40 ? "moderate" : "low",
      stability: hops <= 3 ? "high" : hops <= 5 ? "moderate" : "weak",
    };
  }

  private countRoutes(): number {
    const online = this.nodes.filter((n) => n.status === "online");
    let count = 0;
    for (let i = 0; i < online.length; i++) {
      for (let j = i + 1; j < online.length; j++) {
        if (this.findRouteBetween(online[i].id, online[j].id)) count++;
      }
    }
    return Math.min(count, 7);
  }

  getSnapshot() {
    return {
      status: this.getStatus(),
      nodes: this.getNodes(),
      edges: this.getEdges(),
      messages: this.getMessages(),
      centerpiece: this.getCenterpiece(),
      eventLog: this.eventLog.slice(0, 20),
    };
  }
}
