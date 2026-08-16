import { motion } from "motion/react";

const mapNodes = [
  { x: 80, y: 60, type: "active" },
  { x: 150, y: 90, type: "active" },
  { x: 220, y: 50, type: "sos" },
  { x: 180, y: 140, type: "active" },
  { x: 260, y: 120, type: "weak" },
  { x: 120, y: 160, type: "active" },
  { x: 300, y: 80, type: "gateway" },
];

export default function OfflineMap() {
  return (
    <section className="section-paper py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <div className="grid gap-12 lg:grid-cols-2 lg:items-center">
          <div>
            <p className="label-meta label-meta-dark mb-4">Offline Map</p>
            <h2 className="font-display text-[clamp(2rem,5vw,3.5rem)] leading-tight text-ink">
              See the network
              <br />
              where the map still works.
            </h2>
            <div className="mt-8 flex flex-wrap gap-6">
              {[
                { label: "14 nodes", color: "text-sage" },
                { label: "3 SOS", color: "text-sos" },
                { label: "2 disconnected zones", color: "text-olive-brown" },
                { label: "1 gateway", color: "text-gold" },
              ].map((s) => (
                <span key={s.label} className={`font-sans text-xs tracking-wide ${s.color}`}>
                  {s.label}
                </span>
              ))}
            </div>
          </div>

          <div className="relative border border-olive-brown/20 bg-ink p-6 md:p-8">
            <svg viewBox="0 0 380 220" className="w-full" aria-label="Stylized offline disaster map">
              {/* Roads */}
              <path d="M20 110 H360 M60 20 V200 M200 20 V200 M300 40 V180" stroke="#5B4F32" strokeWidth="0.8" fill="none" opacity="0.4" />
              {/* Buildings */}
              {[
                [40, 40, 30, 25], [100, 30, 20, 35], [240, 50, 25, 20],
                [50, 130, 35, 30], [270, 150, 40, 25],
              ].map(([x, y, w, h], i) => (
                <rect key={i} x={x} y={y} width={w} height={h} fill="#523117" opacity="0.3" />
              ))}
              {/* Rescue zone */}
              <ellipse cx="190" cy="110" rx="70" ry="50" fill="none" stroke="#768965" strokeWidth="1" strokeDasharray="4 3" opacity="0.5" />
              <text x="190" y="115" textAnchor="middle" fill="#768965" fontSize="7" fontFamily="Inter" opacity="0.6">
                RESCUE ZONE
              </text>
              {/* Disconnected zone */}
              <rect x="10" y="170" width="80" height="40" fill="#8B3A2A" opacity="0.1" stroke="#8B3A2A" strokeWidth="0.5" strokeDasharray="3 2" />
              <text x="50" y="195" textAnchor="middle" fill="#8B3A2A" fontSize="6" fontFamily="Inter" opacity="0.5">
                DISCONNECTED
              </text>
              {/* Nodes */}
              {mapNodes.map((n, i) => (
                <motion.circle
                  key={i}
                  cx={n.x}
                  cy={n.y}
                  r="5"
                  fill={
                    n.type === "sos" ? "#8B3A2A" :
                    n.type === "gateway" ? "#D7A84A" :
                    n.type === "weak" ? "#5B4F32" : "#768965"
                  }
                  initial={{ scale: 0 }}
                  whileInView={{ scale: 1 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.1 }}
                />
              ))}
            </svg>
            <p className="label-meta mt-4 text-stone/40">Stylized Map · Demo Data</p>
          </div>
        </div>
      </div>
    </section>
  );
}
