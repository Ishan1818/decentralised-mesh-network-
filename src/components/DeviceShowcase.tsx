import { motion } from "motion/react";

function PhoneScreen() {
  return (
    <div className="flex h-full flex-col bg-ink p-4 text-paper">
      <div className="flex items-center justify-between">
        <span className="font-sans text-[0.5rem] tracking-[0.2em] uppercase">Relay</span>
        <span className="h-2 w-2 rounded-full bg-sage" />
      </div>
      <p className="mt-4 font-sans text-[0.45rem] tracking-[0.15em] text-sage uppercase">
        Mesh Active
      </p>
      <div className="mt-2 flex gap-3 font-sans text-[0.4rem] text-stone/70">
        <span>12 nodes</span>
        <span>3 routes</span>
        <span className="text-sos">1 SOS</span>
      </div>
      <div className="my-3 h-px bg-stone/20" />
      <p className="font-sans text-[0.4rem] tracking-[0.1em] text-stone/50 uppercase">Network</p>
      <svg viewBox="0 0 120 80" className="mt-2 flex-1 text-sage">
        <circle cx="60" cy="15" r="4" fill="currentColor" />
        <circle cx="30" cy="40" r="4" fill="currentColor" />
        <circle cx="90" cy="40" r="4" fill="currentColor" />
        <circle cx="60" cy="55" r="4" fill="currentColor" />
        <circle cx="60" cy="75" r="4" fill="#D7A84A" />
        <line x1="60" y1="15" x2="30" y2="40" stroke="currentColor" strokeWidth="0.8" />
        <line x1="60" y1="15" x2="90" y2="40" stroke="currentColor" strokeWidth="0.8" />
        <line x1="30" y1="40" x2="60" y2="55" stroke="currentColor" strokeWidth="0.8" />
        <line x1="90" y1="40" x2="60" y2="55" stroke="currentColor" strokeWidth="0.8" />
        <line x1="60" y1="55" x2="60" y2="75" stroke="#D7A84A" strokeWidth="0.8" />
      </svg>
    </div>
  );
}

function DeviceFrame({ className, children }: { className?: string; children: React.ReactNode }) {
  return (
    <div className={`relative overflow-hidden rounded-2xl border border-stone/20 bg-ink shadow-2xl ${className}`}>
      <div className="absolute top-0 left-1/2 z-10 h-4 w-20 -translate-x-1/2 rounded-b-lg bg-ink" />
      {children}
    </div>
  );
}

export default function DeviceShowcase() {
  return (
    <section id="how-it-works" className="section-ink relative overflow-hidden py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <p className="label-meta mb-4">Device Experience</p>
        <h2 className="font-display text-[clamp(2rem,5vw,4rem)] leading-tight text-paper">
          One interface.
          <br />
          <span className="text-stone">Every device.</span>
        </h2>

        <div className="relative mt-16 flex items-end justify-center gap-4 md:gap-8">
          {/* Tablet */}
          <motion.div
            className="hidden md:block"
            initial={{ opacity: 0, y: 40 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ delay: 0.2 }}
          >
            <DeviceFrame className="h-48 w-36 -rotate-6 opacity-60">
              <PhoneScreen />
            </DeviceFrame>
          </motion.div>

          {/* Phone - main */}
          <motion.div
            initial={{ opacity: 0, y: 60 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="z-10"
          >
            <DeviceFrame className="h-72 w-40 md:h-80 md:w-44">
              <PhoneScreen />
            </DeviceFrame>
          </motion.div>

          {/* Desktop hint */}
          <motion.div
            className="hidden lg:block"
            initial={{ opacity: 0, y: 40 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ delay: 0.3 }}
          >
            <div className="h-40 w-56 rotate-3 rounded border border-stone/15 bg-ink/80 p-3 opacity-50">
              <div className="h-full border border-stone/10 p-2">
                <PhoneScreen />
              </div>
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
}

export function MobilePhoneSection() {
  const features = [
    "Offline-first.",
    "Peer-to-peer.",
    "Store-and-forward.",
    "Location-aware.",
    "Battery-conscious.",
  ];

  return (
    <section className="section-paper py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <div className="grid gap-16 lg:grid-cols-2 lg:items-center">
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            whileInView={{ opacity: 1, x: 0 }}
            viewport={{ once: true }}
            className="flex justify-center"
          >
            <DeviceFrame className="h-96 w-52">
              <PhoneScreen />
            </DeviceFrame>
          </motion.div>

          <div>
            <h2 className="font-display text-[clamp(2rem,5vw,3.5rem)] leading-tight text-ink">
              Designed for the
              <br />
              moment everything
              <br />
              else stops working.
            </h2>
            <p className="mt-6 font-sans text-sm text-olive-brown">Built for the field.</p>
            <ul className="mt-8 space-y-4">
              {features.map((f) => (
                <li
                  key={f}
                  className="font-sans text-[0.7rem] tracking-[0.12em] text-ink uppercase"
                >
                  {f}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
}
