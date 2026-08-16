import { motion } from "motion/react";
import type { NetworkCardData } from "../data/demoNetwork";

interface NetworkCardProps {
  data: NetworkCardData;
}

function CardTopology({ nodes, edges }: { nodes: { x: number; y: number }[]; edges: [number, number][] }) {
  return (
    <svg viewBox="0 0 100 100" className="h-24 w-full text-sage/60" aria-hidden="true">
      {edges.map(([a, b], i) => (
        <line
          key={i}
          x1={nodes[a]?.x ?? 0}
          y1={nodes[a]?.y ?? 0}
          x2={nodes[b]?.x ?? 0}
          y2={nodes[b]?.y ?? 0}
          stroke="currentColor"
          strokeWidth="0.8"
        />
      ))}
      {nodes.map((n, i) => (
        <circle key={i} cx={n.x} cy={n.y} r="3" fill="currentColor" />
      ))}
    </svg>
  );
}

export default function NetworkCard({ data }: NetworkCardProps) {
  return (
    <motion.article
      className="group relative flex flex-col overflow-hidden border border-olive-brown/20 bg-ink p-6 transition-all duration-300 hover:-translate-y-1.5 hover:border-gold/30 hover:shadow-[0_12px_40px_rgba(16,12,5,0.4)]"
      whileHover={{ scale: 1.01 }}
      layout
    >
      <div className="absolute inset-0 bg-gradient-to-br from-amber/5 to-transparent opacity-0 transition-opacity group-hover:opacity-100" />

      <p className="label-meta relative text-stone/50">{data.title}</p>

      <div className="relative my-6 flex-1">
        <CardTopology nodes={data.nodes} edges={data.edges} />
      </div>

      <div className="relative">
        <p className="font-display text-xl text-paper">{data.metric}</p>
        <p className="mt-1 font-sans text-[0.7rem] text-stone/60">{data.submetric}</p>
        <p className="mt-3 font-sans text-xs leading-relaxed text-stone/50 opacity-0 transition-opacity group-hover:opacity-100">
          {data.description}
        </p>
        <span className="mt-4 inline-block font-sans text-[0.6rem] tracking-[0.15em] text-gold uppercase opacity-0 transition-opacity group-hover:opacity-100">
          Explore →
        </span>
      </div>
    </motion.article>
  );
}
