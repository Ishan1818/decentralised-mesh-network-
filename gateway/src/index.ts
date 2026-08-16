import { serve } from "@hono/node-server";
import { Hono } from "hono";
import { cors } from "hono/cors";
import { WebSocketServer, WebSocket } from "ws";
import type { Server } from "node:http";
import { MeshSimulator } from "./mesh/MeshSimulator.js";

const PORT = Number(process.env.PORT ?? 4000);
const sim = new MeshSimulator();

const app = new Hono();
app.use("/*", cors({ origin: "*" }));

app.get("/api/health", (c) => c.json({ ok: true, service: "relay-gateway" }));

app.get("/api/network/status", (c) => c.json(sim.getStatus()));

app.get("/api/nodes", (c) => c.json({ nodes: sim.getNodes(), edges: sim.getEdges() }));

app.get("/api/messages", (c) => c.json({ messages: sim.getMessages() }));

app.get("/api/events", (c) => c.json({ events: sim.getEventLog() }));

app.get("/api/centerpiece", (c) => c.json(sim.getCenterpiece()));

app.get("/api/routes/:targetId", (c) => {
  const route = sim.getRouteToNode(c.req.param("targetId"));
  if (!route) return c.json({ error: "No route found" }, 404);
  return c.json(route);
});

app.post("/api/simulate/disable", async (c) => {
  const body = await c.req.json<{ nodeId: string; mode?: "centerpiece" | "network" }>();
  if (body.mode === "centerpiece") {
    return c.json(sim.disableCenterpieceNode(body.nodeId));
  }
  return c.json(sim.disableNode(body.nodeId));
});

app.post("/api/simulate/sos", async (c) => {
  let from: string | undefined;
  let to: string | undefined;
  try {
    const body = await c.req.json<{ from?: string; to?: string }>();
    from = body.from;
    to = body.to;
  } catch {
    // empty body is fine
  }
  return c.json(sim.sendSos(from, to));
});

app.post("/api/simulate/reset", (c) => c.json(sim.reset()));

app.get("/api/snapshot", (c) => c.json(sim.getSnapshot()));

const server = serve({ fetch: app.fetch, port: PORT }, (info) => {
  console.log(`RELAY Gateway running on http://localhost:${info.port}`);
  console.log(`WebSocket: ws://localhost:${info.port}/ws`);
}) as Server;

const wss = new WebSocketServer({ server, path: "/ws" });
const clients = new Set<WebSocket>();

wss.on("connection", (ws) => {
  clients.add(ws);
  ws.send(JSON.stringify({ type: "snapshot", data: sim.getSnapshot() }));
  ws.on("close", () => clients.delete(ws));
});

sim.on("update", (data) => {
  const msg = JSON.stringify({ type: "update", data });
  for (const client of clients) {
    if (client.readyState === WebSocket.OPEN) client.send(msg);
  }
});

sim.on("event", (event) => {
  const msg = JSON.stringify({ type: "event", data: event });
  for (const client of clients) {
    if (client.readyState === WebSocket.OPEN) client.send(msg);
  }
});
