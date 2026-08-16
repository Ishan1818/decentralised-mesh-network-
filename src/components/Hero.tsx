import { useEffect } from "react";
import { motion } from "motion/react";
import NetworkStatusCard from "./NetworkStatusCard";

const heroImage =
  "https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=1920&q=80&auto=format&fit=crop";

export default function Hero() {
  useEffect(() => {
    document.documentElement.style.setProperty("--hero-loaded", "1");
  }, []);

  return (
    <section className="relative min-h-screen overflow-hidden" aria-label="Hero">
      <motion.div
        className="absolute inset-0"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 1.4, ease: "easeOut" }}
      >
        <img
          src={heroImage}
          alt="Urban landscape after infrastructure failure, dawn light over damaged city"
          className="image-warm h-full w-full object-cover"
          fetchPriority="high"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-ink/50 via-ink/30 to-ink/80" />
        <div className="absolute inset-0 bg-gradient-to-r from-ink/60 via-transparent to-transparent" />
      </motion.div>

      <div className="relative z-10 mx-auto flex min-h-screen max-w-[1440px] flex-col justify-end px-6 pb-16 pt-32 md:px-10 md:pb-24 lg:px-14 lg:pb-32">
        <div className="grid gap-12 lg:grid-cols-12 lg:items-end">
          <div className="lg:col-span-7">
            <motion.p
              className="label-meta mb-6 text-stone/80"
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4, duration: 0.6 }}
            >
              When infrastructure disappears, people become the network.
            </motion.p>

            <motion.h1
              className="font-display text-[clamp(2.8rem,8vw,6.5rem)] leading-[0.95] font-medium text-paper drop-shadow-[0_2px_24px_rgba(16,12,5,0.8)]"
              initial="hidden"
              animate="visible"
              variants={{
                hidden: {},
                visible: { transition: { staggerChildren: 0.15, delayChildren: 0.5 } },
              }}
            >
              <motion.span
                className="block"
                variants={{ hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0 } }}
                transition={{ duration: 0.7 }}
              >
                Infrastructure fails.
              </motion.span>
              <motion.span
                className="mt-2 block text-stone"
                variants={{ hidden: { opacity: 0, y: 30 }, visible: { opacity: 1, y: 0 } }}
                transition={{ duration: 0.7 }}
              >
                People connect.
              </motion.span>
            </motion.h1>

            <motion.div
              className="mt-10 flex flex-wrap items-center gap-4"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 1.1, duration: 0.6 }}
            >
              <a href="#network" className="btn-primary">
                Explore the Mesh <span className="arrow">→</span>
              </a>
              <a href="#how-it-works" className="btn-secondary">
                See How It Works
              </a>
            </motion.div>

            <motion.div
              className="mt-12 flex flex-wrap gap-6"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 1.3, duration: 0.6 }}
            >
              {[
                { icon: "+", label: "Explore network" },
                { icon: "○", label: "Live topology" },
                { icon: "⌁", label: "Field simulation" },
              ].map((ctrl) => (
                <button
                  key={ctrl.label}
                  type="button"
                  className="flex items-center gap-2 border-0 bg-transparent font-sans text-[0.6rem] tracking-[0.14em] text-stone/70 uppercase cursor-pointer transition-colors hover:text-paper"
                >
                  <span className="text-gold">{ctrl.icon}</span>
                  {ctrl.label}
                </button>
              ))}
            </motion.div>
          </div>

          <motion.div
            className="lg:col-span-5 max-lg:mt-4"
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.9, duration: 0.7 }}
          >
            <NetworkStatusCard />
          </motion.div>
        </div>
      </div>
    </section>
  );
}
