import { centerpieceNodes, centerpieceEdges } from "../data/demoNodes";
import { useNetworkAnimation } from "../hooks/useNetworkAnimation";
import { useMeshOptional } from "../context/MeshContext";
import { motion } from "motion/react";
import { useState } from "react";

export default function FailureSimulation() {
  const mesh = useMeshOptional();
  const nodes = centerpieceNodes;
  const edges = centerpieceEdges;

  const local = useNetworkAnimation({ nodes, edges });
  const [showSos, setShowSos] = useState(false);

  const centerpiece = mesh?.snapshot?.centerpiece;
  const useLive = mesh?.connected && centerpiece;

  const excludedNodes = useLive
    ? new Set(centerpiece.nodes.filter((n) => n.disabled).map((n) => n.id))
    : local.excludedNodes;

  const activeRoute = useLive ? centerpiece.activeRoute : local.activeRoute;
  const status = useLive ? centerpiece.status : local.status;
  const activeEdges = useLive
    ? centerpiece.edges
    : local.activeEdges;

  const cDisabled = excludedNodes.has("C");

  const handleDisableC = async () => {
    if (cDisabled) return;
    if (useLive && mesh) {
      await mesh.disableNode("C", "centerpiece");
    } else {
      local.simulateFailure("C", "A", "F");
    }
  };

  const handleSendSOS = async () => {
    if (useLive && mesh) {
      await mesh.sendSos();
      setShowSos(true);
      setTimeout(() => setShowSos(false), 3000);
    } else {
      const route = activeRoute.length > 1 ? activeRoute : ["A", "D", "E", "F"];
      local.sendPacket(route);
    }
  };

  const handleReset = async () => {
    if (useLive && mesh) {
      await mesh.reset();
    } else {
      local.resetNetwork();
    }
    setShowSos(false);
  };

  const nodeMap = new Map(nodes.map((n) => [n.id, n]));
  const getPos = (id: string) => nodeMap.get(id);

  return (
    <section id="demo" className="section-ink py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <div className="grid gap-12 lg:grid-cols-2 lg:items-center">
          <div>
            <p className="label-meta mb-4">Core Demonstration</p>
            <h2 className="font-display text-[clamp(2rem,5vw,4rem)] leading-tight text-paper">
              Networks break.
              <br />
              <span className="text-stone">Routes don't have to.</span>
            </h2>
            <p className="mt-6 max-w-md font-sans text-sm leading-relaxed text-stone/70">
              Disable a node and watch the mesh recalculate. Send an SOS packet through
              the recovered path.
            </p>

            {mesh?.connected && (
              <p className="mt-4 font-sans text-[0.6rem] tracking-[0.12em] text-sage uppercase">
                ● Connected to gateway
              </p>
            )}

            <div className="mt-8 flex flex-wrap gap-3">
              <button
                type="button"
                onClick={handleDisableC}
                disabled={cDisabled}
                className="btn-primary disabled:opacity-40 disabled:cursor-not-allowed"
              >
                Disable Node C <span className="arrow">→</span>
              </button>
              <button type="button" onClick={handleSendSOS} className="btn-secondary">
                Send SOS
              </button>
              <button type="button" onClick={handleReset} className="btn-secondary">
                Reset
              </button>
            </div>

            {status && (
              <motion.p
                className="mt-6 font-sans text-[0.7rem] tracking-[0.15em] text-gold uppercase"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                key={status}
              >
                {status}
              </motion.p>
            )}

            {activeRoute.length > 1 && (
              <p className="mt-4 font-sans text-sm text-paper">
                {activeRoute.join(" → ")}
              </p>
            )}
          </div>

          <div className="relative border border-stone/10 bg-ink/60 p-6 md:p-10">
            <svg viewBox="0 0 700 380" className="w-full" aria-label="Failure simulation topology">
              {activeEdges.map(([a, b]) => {
                const na = getPos(a);
                const nb = getPos(b);
                if (!na || !nb) return null;
                const onRoute =
                  activeRoute.includes(a) &&
                  activeRoute.includes(b) &&
                  Math.abs(activeRoute.indexOf(a) - activeRoute.indexOf(b)) === 1;
                return (
                  <line
                    key={`${a}-${b}`}
                    x1={na.x}
                    y1={na.y}
                    x2={nb.x}
                    y2={nb.y}
                    stroke={onRoute ? "#D7A84A" : "#5B4F32"}
                    strokeWidth={onRoute ? 2.5 : 1}
                    opacity={onRoute ? 1 : 0.45}
                  />
                );
              })}

              {nodes.map((node) => {
                const disabled = excludedNodes.has(node.id);
                const onRoute = activeRoute.includes(node.id);
                return (
                  <g key={node.id}>
                    <circle
                      cx={node.x}
                      cy={node.y}
                      r={12}
                      fill={
                        disabled
                          ? "transparent"
                          : onRoute
                            ? "#9A632C"
                            : "#768965"
                      }
                      stroke={disabled ? "#8B3A2A" : onRoute ? "#D7A84A" : "#5B4F32"}
                      strokeWidth={disabled ? 1.5 : 1}
                      strokeDasharray={disabled ? "4 3" : undefined}
                    />
                    {disabled && (
                      <text
                        x={node.x}
                        y={node.y + 4}
                        textAnchor="middle"
                        fill="#8B3A2A"
                        fontSize="14"
                        fontFamily="Inter"
                      >
                        ✕
                      </text>
                    )}
                    <text
                      x={node.x}
                      y={node.y + 28}
                      textAnchor="middle"
                      fill="#C5BDA4"
                      fontSize="11"
                      fontFamily="Inter"
                      fontWeight="500"
                    >
                      {node.label}
                    </text>
                  </g>
                );
              })}

              {showSos && activeRoute.length > 0 && (
                <motion.text
                  x={getPos(activeRoute[activeRoute.length - 1])!.x}
                  y={getPos(activeRoute[activeRoute.length - 1])!.y - 20}
                  textAnchor="middle"
                  fill="#D7A84A"
                  fontSize="10"
                  fontFamily="Inter"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: [1, 0.5, 1] }}
                  transition={{ repeat: Infinity, duration: 0.8 }}
                >
                  🚨 SOS
                </motion.text>
              )}
            </svg>

            <p className="label-meta mt-4 text-center text-stone/25">
              {useLive ? "Live Gateway Simulation" : "Local Demo · Offline Fallback"}
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
