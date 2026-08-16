import { useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import { networkCards, filterLabels, type NetworkFilter } from "../data/demoNetwork";
import NetworkCard from "./NetworkCard";

export default function NetworkGrid() {
  const [filter, setFilter] = useState<NetworkFilter>("all");

  const filtered =
    filter === "all"
      ? networkCards
      : networkCards.filter((c) => c.filter.includes(filter));

  return (
    <section className="section-paper py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <p className="label-meta label-meta-dark mb-4">Explore the Network</p>
        <h2 className="font-display text-[clamp(2rem,5vw,4rem)] leading-tight text-ink">
          Network at a glance
        </h2>

        <div className="mt-10 flex flex-wrap gap-2" role="tablist" aria-label="Filter network">
          {filterLabels.map((f) => (
            <button
              key={f.id}
              type="button"
              role="tab"
              aria-selected={filter === f.id}
              onClick={() => setFilter(f.id)}
              className={`border px-4 py-2 font-sans text-[0.6rem] tracking-[0.12em] uppercase transition-colors cursor-pointer ${
                filter === f.id
                  ? "border-ink bg-ink text-paper"
                  : "border-olive-brown/25 text-olive-brown hover:border-ink"
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>

        <motion.div
          className="mt-12 grid gap-4 sm:grid-cols-2 lg:grid-cols-3"
          layout
        >
          <AnimatePresence mode="popLayout">
            {filtered.map((card) => (
              <motion.div
                key={card.id}
                layout
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.95 }}
                transition={{ duration: 0.3 }}
              >
                <NetworkCard data={card} />
              </motion.div>
            ))}
          </AnimatePresence>
        </motion.div>

        <p className="label-meta label-meta-dark mt-8 opacity-40">Demo Network · Simulation Data</p>
      </div>
    </section>
  );
}
