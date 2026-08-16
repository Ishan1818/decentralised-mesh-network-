import { useState } from "react";
import { motion } from "motion/react";
import { meshHeroNodes, meshHeroEdges } from "../data/demoNetwork";
import { useNetworkAnimation } from "../hooks/useNetworkAnimation";

export default function MeshGraph() {
  const nodes = meshHeroNodes.map((n) => ({
    id: n.id,
    label: n.label,
    x: n.x,
    y: n.y,
    battery: n.battery,
    neighbors: n.neighbors,
  }));

  const {
    activeRoute,
    packetPosition,
    hoveredNode,
    activeEdges,
    setHoveredNode,
    highlightRoute,
    sendPacket,
  } = useNetworkAnimation({ nodes, edges: meshHeroEdges });

  const [selectedRoute, setSelectedRoute] = useState<string[]>([]);

  const handleNodeClick = (nodeId: string) => {
    if (!selectedRoute.length) {
      setSelectedRoute([nodeId]);
      return;
    }
    if (selectedRoute.length === 1 && selectedRoute[0] !== nodeId) {
      const route = highlightRoute(selectedRoute[0], nodeId);
      if (route) setSelectedRoute(route);
      return;
    }
    setSelectedRoute([nodeId]);
  };

  const nodeMap = new Map(nodes.map((n) => [n.id, n]));
  const hovered = hoveredNode ? nodeMap.get(hoveredNode) : null;

  const getNodePos = (id: string) => nodeMap.get(id);

  const isOnRoute = (id: string) => activeRoute.includes(id);
  const isPacketHere = (id: string, idx: number) =>
    packetPosition >= 0 && activeRoute[packetPosition] === id;

  return (
    <section id="network" className="section-ink relative overflow-hidden py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <div className="mb-12 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <p className="label-meta mb-4">Live Mesh Visualization</p>
            <h2 className="font-display text-[clamp(2rem,5vw,4rem)] leading-tight text-paper">
              Discover the network
            </h2>
          </div>
          <p className="max-w-sm font-sans text-sm leading-relaxed text-stone/70">
            Hover nodes to inspect status. Click two nodes to trace a route.{" "}
            <span className="text-gold">Demo data.</span>
          </p>
        </div>

        <div className="relative rounded-sm border border-stone/10 bg-ink/50 p-4 md:p-8">
          <svg
            viewBox="0 0 600 380"
            className="w-full"
            role="img"
            aria-label="Interactive mesh network graph"
          >
            <defs>
              <radialGradient id="nodeGlow" cx="50%" cy="50%" r="50%">
                <stop offset="0%" stopColor="#D7A84A" stopOpacity="0.4" />
                <stop offset="100%" stopColor="#D7A84A" stopOpacity="0" />
              </radialGradient>
            </defs>

            {activeEdges.map(([a, b]) => {
              const na = getNodePos(a);
              const nb = getNodePos(b);
              if (!na || !nb) return null;
              const onRoute =
                activeRoute.includes(a) && activeRoute.includes(b) &&
                Math.abs(activeRoute.indexOf(a) - activeRoute.indexOf(b)) === 1;
              return (
                <motion.line
                  key={`${a}-${b}`}
                  x1={na.x}
                  y1={na.y}
                  x2={nb.x}
                  y2={nb.y}
                  stroke={onRoute ? "#D7A84A" : "#5B4F32"}
                  strokeWidth={onRoute ? 2 : 1}
                  opacity={onRoute ? 1 : 0.5}
                  initial={{ pathLength: 0 }}
                  animate={{ pathLength: 1 }}
                  transition={{ duration: 0.8 }}
                />
              );
            })}

            {nodes.map((node) => {
              const onRoute = isOnRoute(node.id);
              const hasPacket = activeRoute.some(
                (id, i) => id === node.id && i === packetPosition
              );
              return (
                <g key={node.id}>
                  {onRoute && (
                    <circle cx={node.x} cy={node.y} r="18" fill="url(#nodeGlow)" />
                  )}
                  <motion.circle
                    cx={node.x}
                    cy={node.y}
                    r={hoveredNode === node.id ? 10 : 7}
                    fill={hasPacket ? "#D7A84A" : onRoute ? "#9A632C" : "#768965"}
                    stroke={hoveredNode === node.id ? "#E8E6D5" : "transparent"}
                    strokeWidth="1.5"
                    className="cursor-pointer"
                    onMouseEnter={() => setHoveredNode(node.id)}
                    onMouseLeave={() => setHoveredNode(null)}
                    onClick={() => handleNodeClick(node.id)}
                    whileHover={{ scale: 1.15 }}
                  />
                  <text
                    x={node.x}
                    y={node.y + 22}
                    textAnchor="middle"
                    fill="#C5BDA4"
                    fontSize="10"
                    fontFamily="Inter, sans-serif"
                    letterSpacing="0.1em"
                  >
                    {node.label}
                  </text>
                </g>
              );
            })}
          </svg>

          {hovered && (
            <motion.div
              className="editorial-card absolute right-4 top-4 p-4 md:right-8 md:top-8 md:p-6"
              initial={{ opacity: 0, x: 10 }}
              animate={{ opacity: 1, x: 0 }}
            >
              <p className="label-meta mb-2">Node {hovered.label}</p>
              <p className="font-sans text-xs tracking-widest text-sage uppercase">Online</p>
              <ul className="mt-3 space-y-1 font-sans text-[0.7rem] text-stone/70">
                <li>Battery {hovered.battery}%</li>
                <li>{hovered.neighbors} neighbors</li>
                <li>Relay enabled</li>
              </ul>
            </motion.div>
          )}

          {activeRoute.length > 1 && (
            <div className="mt-4 flex flex-wrap items-center gap-4 border-t border-stone/10 pt-4">
              <p className="font-sans text-[0.65rem] tracking-[0.15em] text-gold uppercase">
                Active Route
              </p>
              <p className="font-sans text-sm text-paper">
                {activeRoute.map((id) => nodeMap.get(id)?.label ?? id).join(" → ")}
              </p>
              <button
                type="button"
                className="btn-primary ml-auto text-[0.55rem]"
                onClick={() => sendPacket(activeRoute)}
              >
                Send Packet <span className="arrow">→</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
