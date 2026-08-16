import { motion } from "motion/react";

function MiniTopology() {
  return (
    <svg viewBox="0 0 80 50" className="h-12 w-20 text-gold/80" aria-hidden="true">
      <circle cx="15" cy="10" r="3" fill="currentColor" />
      <circle cx="65" cy="10" r="3" fill="currentColor" />
      <circle cx="40" cy="40" r="3" fill="currentColor" />
      <circle cx="65" cy="40" r="3" fill="currentColor" />
      <line x1="15" y1="10" x2="65" y2="10" stroke="currentColor" strokeWidth="0.75" opacity="0.5" />
      <line x1="15" y1="10" x2="40" y2="40" stroke="currentColor" strokeWidth="0.75" opacity="0.5" />
      <line x1="65" y1="10" x2="65" y2="40" stroke="currentColor" strokeWidth="0.75" opacity="0.5" />
      <line x1="40" y1="40" x2="65" y2="40" stroke="currentColor" strokeWidth="0.75" opacity="0.5" />
    </svg>
  );
}

export default function NetworkStatusCard() {
  return (
    <motion.aside
      className="editorial-card p-5 md:p-8 max-lg:max-w-md"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: 1, duration: 0.6 }}
      aria-label="Network status"
    >
      <p className="label-meta mb-4 text-stone/60">Network Status</p>

      <div className="mb-6 flex items-start justify-between">
        <div>
          <p className="font-sans text-[0.65rem] tracking-[0.2em] text-stone/50 uppercase">
            Local Mesh
          </p>
          <p className="font-display mt-1 text-3xl text-gold">Active</p>
        </div>
        <MiniTopology />
      </div>

      <p className="font-display mb-6 text-2xl text-paper">14 nodes nearby</p>

      <ul className="space-y-2 border-t border-stone/10 pt-4">
        {["No internet", "No cellular service", "Communication available"].map((item, i) => (
          <li
            key={item}
            className={`font-sans text-[0.7rem] tracking-wide ${
              i === 2 ? "text-sage" : "text-stone/60"
            }`}
          >
            {i === 2 ? "● " : "○ "}
            {item}
          </li>
        ))}
      </ul>

      <p className="label-meta mt-6 text-stone/25">Demo Network</p>
    </motion.aside>
  );
}
