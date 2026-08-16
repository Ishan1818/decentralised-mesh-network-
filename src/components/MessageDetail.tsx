import { motion } from "motion/react";
import { sosMessage } from "../data/demoMessages";

export default function MessageDetail() {
  return (
    <section className="section-paper py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <div className="grid gap-12 lg:grid-cols-2">
          <div>
            <p className="label-meta label-meta-dark mb-2">{sosMessage.id}</p>
            <h2 className="font-display text-[clamp(2rem,4vw,3rem)] text-ink">
              {sosMessage.title}
            </h2>
            <p className="mt-2 font-sans text-[0.65rem] tracking-[0.2em] text-olive-brown uppercase">
              {sosMessage.sector}
            </p>

            <div className="mt-10 grid grid-cols-2 gap-6">
              <div>
                <p className="label-meta label-meta-dark">Priority</p>
                <p className="mt-1 font-sans text-sm text-sos">{sosMessage.priority}</p>
              </div>
              <div>
                <p className="label-meta label-meta-dark">Status</p>
                <p className="mt-1 font-sans text-sm text-sage">{sosMessage.status}</p>
              </div>
              <div className="col-span-2">
                <p className="label-meta label-meta-dark">Location</p>
                <p className="mt-1 font-sans text-sm text-ink">
                  {sosMessage.location.lat}, {sosMessage.location.lng}
                </p>
              </div>
            </div>
          </div>

          <div className="border border-olive-brown/20 bg-ink p-8">
            <p className="label-meta text-stone/50">Route</p>
            <div className="mt-6 space-y-3">
              {sosMessage.route.map((node, i) => (
                <motion.div
                  key={node}
                  className="flex items-center gap-3"
                  initial={{ opacity: 0, x: -10 }}
                  whileInView={{ opacity: 1, x: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.15 }}
                >
                  <span className="font-sans text-sm text-paper">{node}</span>
                  {i < sosMessage.route.length - 1 && (
                    <span className="text-gold">↓</span>
                  )}
                </motion.div>
              ))}
            </div>

            <div className="mt-10 border-t border-stone/10 pt-6">
              <p className="label-meta text-sage">{sosMessage.status}</p>
              <p className="mt-1 font-sans text-sm text-stone/70">
                {sosMessage.deliveredAt}
              </p>
            </div>

            <svg viewBox="0 0 300 60" className="mt-8 w-full" aria-hidden="true">
              {sosMessage.route.map((_, i) => {
                if (i === 0) return null;
                const x1 = (i - 1) * 70 + 30;
                const x2 = i * 70 + 30;
                return (
                  <g key={i}>
                    <line x1={x1} y1="30" x2={x2} y2="30" stroke="#D7A84A" strokeWidth="1.5" />
                    <circle cx={x1} cy="30" r="5" fill="#768965" />
                    {i === sosMessage.route.length - 1 && (
                      <circle cx={x2} cy="30" r="5" fill="#D7A84A" />
                    )}
                  </g>
                );
              })}
            </svg>
          </div>
        </div>
      </div>
    </section>
  );
}
