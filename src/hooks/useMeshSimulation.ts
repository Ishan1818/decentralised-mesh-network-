import { useCallback, useEffect, useState } from "react";

export interface SimNode {
  id: string;
  x: number;
  y: number;
  active: boolean;
  pulse: number;
}

export function useMeshSimulation(nodeCount = 6) {
  const [nodes, setNodes] = useState<SimNode[]>([]);
  const [connections, setConnections] = useState<[number, number][]>([]);

  useEffect(() => {
    const baseNodes: SimNode[] = Array.from({ length: nodeCount }, (_, i) => ({
      id: `sim-${i}`,
      x: 20 + (i % 3) * 30 + Math.random() * 10,
      y: 20 + Math.floor(i / 3) * 35 + Math.random() * 10,
      active: false,
      pulse: 0,
    }));
    setNodes(baseNodes);

    const edges: [number, number][] = [];
    for (let i = 0; i < nodeCount - 1; i++) {
      edges.push([i, i + 1]);
      if (i % 2 === 0 && i + 2 < nodeCount) edges.push([i, i + 2]);
    }
    setConnections(edges);
  }, [nodeCount]);

  const discover = useCallback(() => {
    setNodes((prev) =>
      prev.map((n, i) => ({
        ...n,
        active: true,
        pulse: i * 0.15,
      }))
    );
  }, []);

  return { nodes, connections, discover };
}
