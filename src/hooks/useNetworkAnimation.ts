import { useCallback, useEffect, useRef, useState } from "react";
import { buildGraph, findRoute } from "../utils/routing";
import { getActiveEdges } from "../utils/network";

export interface NetworkNode {
  id: string;
  label: string;
  x: number;
  y: number;
  battery?: number;
  neighbors?: number;
}

export interface UseNetworkAnimationOptions {
  nodes: NetworkNode[];
  edges: [string, string][];
}

export function useNetworkAnimation({ nodes, edges }: UseNetworkAnimationOptions) {
  const [excludedNodes, setExcludedNodes] = useState<Set<string>>(new Set());
  const [activeRoute, setActiveRoute] = useState<string[]>([]);
  const [packetPosition, setPacketPosition] = useState<number>(-1);
  const [status, setStatus] = useState<string>("");
  const [hoveredNode, setHoveredNode] = useState<string | null>(null);
  const packetTimer = useRef<ReturnType<typeof setInterval> | null>(null);

  const activeEdges = getActiveEdges(edges, excludedNodes);
  const graph = buildGraph(activeEdges);

  const clearPacket = useCallback(() => {
    if (packetTimer.current) {
      clearInterval(packetTimer.current);
      packetTimer.current = null;
    }
    setPacketPosition(-1);
  }, []);

  const highlightRoute = useCallback(
    (from: string, to: string) => {
      const route = findRoute(graph, from, to, excludedNodes);
      if (route) {
        setActiveRoute(route);
        return route;
      }
      setActiveRoute([]);
      return null;
    },
    [graph, excludedNodes]
  );

  const removeNode = useCallback(
    (nodeId: string) => {
      setExcludedNodes((prev) => new Set([...prev, nodeId]));
      setActiveRoute([]);
      clearPacket();
    },
    [clearPacket]
  );

  const restoreNode = useCallback((nodeId: string) => {
    setExcludedNodes((prev) => {
      const next = new Set(prev);
      next.delete(nodeId);
      return next;
    });
  }, []);

  const resetNetwork = useCallback(() => {
    setExcludedNodes(new Set());
    setActiveRoute([]);
    clearPacket();
    setStatus("");
  }, [clearPacket]);

  const sendPacket = useCallback(
    (route: string[]) => {
      if (route.length < 2) return;
      clearPacket();
      setActiveRoute(route);
      setPacketPosition(0);
      let step = 0;
      packetTimer.current = setInterval(() => {
        step += 1;
        if (step >= route.length) {
          clearPacket();
          setPacketPosition(route.length - 1);
          setStatus("SOS RECEIVED");
          return;
        }
        setPacketPosition(step);
      }, 600);
    },
    [clearPacket]
  );

  const simulateFailure = useCallback(
    (nodeId: string, from: string, to: string) => {
      setStatus("ROUTE FAILURE DETECTED");
      setActiveRoute([]);
      clearPacket();

      setTimeout(() => {
        setStatus("Finding alternate path...");
        const failed = new Set([...excludedNodes, nodeId]);
        const tempGraph = buildGraph(getActiveEdges(edges, failed));
        const altRoute = findRoute(tempGraph, from, to, failed);
        if (altRoute) {
          setExcludedNodes(failed);
          setActiveRoute(altRoute);
          setStatus(`ROUTE RECOVERED · +${altRoute.length - 2} HOP`);
        } else {
          setStatus("NO ROUTE AVAILABLE");
        }
      }, 800);
    },
    [edges, excludedNodes, clearPacket]
  );

  useEffect(() => () => clearPacket(), [clearPacket]);

  return {
    excludedNodes,
    activeRoute,
    packetPosition,
    status,
    hoveredNode,
    activeEdges,
    setHoveredNode,
    highlightRoute,
    removeNode,
    restoreNode,
    resetNetwork,
    sendPacket,
    simulateFailure,
    clearPacket,
  };
}
