import { centerpieceNodes, centerpieceEdges } from "../data/demoNodes";
import { useNetworkAnimation } from "../hooks/useNetworkAnimation";
import { motion } from "motion/react";

export default function FailureSimulation() {
  const nodes = centerpieceNodes;
  const edges = centerpieceEdges;

  const {
    excludedNodes,
    activeRoute,
    packetPosition,
    status,
    activeEdges,
    simulateFailure,
    sendPacket,
    resetNetwork,
  } = useNetworkAnimation({ nodes, edges });

  const nodeMap = new Map(nodes.map((n) => [n.id, n]));
  const cDisabled = excludedNodes.has("C");

  const handleDisableC = () => {
    if (cDisabled) return;
    simulateFailure("C", "A", "F");
  };

  const handleSendSOS = () => {
    const route = activeRoute.length > 1 ? activeRoute : ["A", "D", "E", "F"];
    sendPacket(route);
  };

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
              <button type="button" onClick={resetNetwork} className="btn-secondary">
                Reset
              </button>
            </div>

            {status && (
              <motion.p
                className="mt-6 font-sans text-[0.7rem] tracking-[0.15em] text-gold uppercase"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
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
                const hasPacket =
                  packetPosition >= 0 && activeRoute[packetPosition] === node.id;
                return (
                  <g key={node.id}>
                    <circle
                      cx={node.x}
                      cy={node.y}
                      r={12}
                      fill={
                        disabled
                          ? "transparent"
                          : hasPacket
                            ? "#D7A84A"
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

              {packetPosition >= 0 && activeRoute[packetPosition] && (
                <motion.text
                  x={getPos(activeRoute[packetPosition])!.x}
                  y={getPos(activeRoute[packetPosition])!.y - 20}
                  textAnchor="middle"
                  fill="#D7A84A"
                  fontSize="10"
                  fontFamily="Inter"
                  animate={{ opacity: [1, 0.5, 1] }}
                  transition={{ repeat: Infinity, duration: 0.8 }}
                >
                  🚨 SOS
                </motion.text>
              )}
            </svg>

            <p className="label-meta mt-4 text-center text-stone/25">
              Interactive Simulation · Demo Data
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
