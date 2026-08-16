export type Graph = Map<string, string[]>;

export function buildGraph(edges: [string, string][]): Graph {
  const graph: Graph = new Map();
  for (const [a, b] of edges) {
    if (!graph.has(a)) graph.set(a, []);
    if (!graph.has(b)) graph.set(b, []);
    graph.get(a)!.push(b);
    graph.get(b)!.push(a);
  }
  return graph;
}

export function getActiveEdges(
  edges: [string, string][],
  excluded: Set<string>
): [string, string][] {
  return edges.filter(([a, b]) => !excluded.has(a) && !excluded.has(b));
}

export function findRoute(
  graph: Graph,
  from: string,
  to: string,
  excluded: Set<string> = new Set()
): string[] | null {
  if (excluded.has(from) || excluded.has(to)) return null;
  if (from === to) return [from];

  const queue = [from];
  const visited = new Set([from]);
  const parent = new Map<string, string>();

  while (queue.length > 0) {
    const current = queue.shift()!;
    for (const neighbor of graph.get(current) ?? []) {
      if (visited.has(neighbor) || excluded.has(neighbor)) continue;
      visited.add(neighbor);
      parent.set(neighbor, current);
      if (neighbor === to) {
        const path: string[] = [to];
        let node = to;
        while (parent.has(node)) {
          node = parent.get(node)!;
          path.unshift(node);
        }
        return path;
      }
      queue.push(neighbor);
    }
  }
  return null;
}
