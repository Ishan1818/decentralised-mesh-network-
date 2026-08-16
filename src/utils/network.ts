import type { Graph } from "./routing";
import { buildGraph } from "./routing";

export function getActiveEdges(
  edges: [string, string][],
  excludedNodes: Set<string>
): [string, string][] {
  return edges.filter(([a, b]) => !excludedNodes.has(a) && !excludedNodes.has(b));
}

export function getGraphFromEdges(
  edges: [string, string][],
  excludedNodes: Set<string> = new Set()
): Graph {
  return buildGraph(getActiveEdges(edges, excludedNodes));
}

export function countHops(route: string[]): number {
  return Math.max(0, route.length - 1);
}
