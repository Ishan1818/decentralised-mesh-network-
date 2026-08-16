import { useEffect, useRef, useState } from "react";
import { motion, useInView } from "motion/react";
import { storeForwardMessage } from "../data/demoMessages";

const bgImage =
  "https://images.unsplash.com/photo-1547036967-23d11aacaee0?w=1920&q=80&auto=format&fit=crop";

export default function StoreForwardStory() {
  const ref = useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { once: false, margin: "-20%" });
  const [step, setStep] = useState(0);

  useEffect(() => {
    if (!inView) return;
    const interval = setInterval(() => {
      setStep((s) => (s < storeForwardMessage.steps.length ? s + 1 : s));
    }, 1200);
    return () => clearInterval(interval);
  }, [inView]);

  useEffect(() => {
    if (!inView) setStep(0);
  }, [inView]);

  return (
    <section ref={ref} className="relative min-h-[80vh] overflow-hidden py-24 md:py-32">
      <img
        src={bgImage}
        alt="Flooded urban area with disconnected infrastructure"
        className="image-warm absolute inset-0 h-full w-full object-cover"
        loading="lazy"
      />
      <div className="absolute inset-0 bg-ink/70" />

      <div className="relative z-10 mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <h2 className="font-display text-[clamp(2rem,5vw,4rem)] leading-tight text-paper">
          Sometimes there is no route.
          <br />
          <span className="text-stone">Yet.</span>
        </h2>

        <div className="mt-16 grid gap-12 lg:grid-cols-2">
          <div className="editorial-card max-w-sm p-6 md:p-8">
            <p className="label-meta text-stone/50">Message</p>
            <p className="mt-4 font-sans text-sm leading-relaxed text-paper">
              {storeForwardMessage.body}
            </p>
            <div className="mt-8 border-t border-stone/10 pt-6">
              <p className="label-meta text-stone/50">Status</p>
              <p className="mt-2 font-sans text-xs tracking-widest text-gold uppercase">
                {step === 0 ? storeForwardMessage.status : storeForwardMessage.steps[step - 1]}
              </p>
            </div>
          </div>

          <div className="flex flex-col justify-center gap-6">
            {storeForwardMessage.steps.map((s, i) => (
              <motion.div
                key={s}
                className={`flex items-center gap-4 ${
                  step > i ? "opacity-100" : "opacity-30"
                }`}
                animate={{ opacity: step > i ? 1 : 0.3 }}
                transition={{ duration: 0.5 }}
              >
                <span className="font-sans text-[0.65rem] tracking-[0.15em] text-gold uppercase">
                  {s}
                </span>
                {i < storeForwardMessage.steps.length - 1 && (
                  <span className="text-stone/40">↓</span>
                )}
              </motion.div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
