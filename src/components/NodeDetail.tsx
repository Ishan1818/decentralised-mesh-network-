import { motion } from "motion/react";
import { nodeDetail } from "../data/demoNodes";

export default function NodeDetail() {
  const stats = [
    { label: "Battery", value: `${nodeDetail.battery}%` },
    { label: "Connectivity", value: nodeDetail.signal },
    { label: "Known Routes", value: String(nodeDetail.routes) },
    { label: "Message Queue", value: String(nodeDetail.queue) },
    { label: "Last Seen", value: nodeDetail.lastSeen },
  ];

  return (
    <section className="section-ink py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <div className="grid gap-12 lg:grid-cols-2">
          <div className="relative flex items-center justify-center border border-stone/10 bg-ink/50 p-8 md:p-12">
            <svg viewBox="0 0 400 300" className="w-full max-w-md" aria-hidden="true">
              <circle cx="200" cy="150" r="40" fill="#9A632C" opacity="0.3" />
              <circle cx="200" cy="150" r="12" fill="#D7A84A" />
              {[
                [120, 80], [280, 80], [100, 200], [300, 200], [200, 60],
              ].map(([x, y], i) => (
                <g key={i}>
                  <line x1="200" y1="150" x2={x} y2={y} stroke="#5B4F32" strokeWidth="1" />
                  <circle cx={x} cy={y} r="6" fill="#768965" />
                </g>
              ))}
              <text x="200" y="155" textAnchor="middle" fill="#E8E6D5" fontSize="10" fontFamily="Inter">
                18
              </text>
            </svg>
          </div>

          <div>
            <p className="label-meta mb-2">{nodeDetail.id}</p>
            <p className="font-sans text-[0.65rem] tracking-[0.2em] text-stone uppercase">
              {nodeDetail.title}
            </p>
            <p className="mt-2 font-sans text-xs tracking-widest text-sage uppercase">
              {nodeDetail.status}
            </p>

            <div className="mt-10 space-y-6">
              <p className="label-meta">Characteristics</p>
              {stats.map((s) => (
                <div key={s.label} className="flex items-baseline justify-between border-b border-stone/10 pb-3">
                  <span className="font-sans text-sm text-stone/70">{s.label}</span>
                  <span className="font-display text-xl text-paper">{s.value}</span>
                </div>
              ))}
            </div>

            <motion.div
              className="mt-12"
              initial={{ opacity: 0 }}
              whileInView={{ opacity: 1 }}
              viewport={{ once: true }}
            >
              <h3 className="font-display text-2xl text-paper">The Story</h3>
              <p className="mt-4 font-sans text-sm leading-relaxed text-stone/70">
                {nodeDetail.story}
              </p>
            </motion.div>
          </div>
        </div>
      </div>
    </section>
  );
}
