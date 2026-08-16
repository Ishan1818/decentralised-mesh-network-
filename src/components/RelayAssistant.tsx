import { useState } from "react";
import { motion, AnimatePresence } from "motion/react";
import { assistantRoutes } from "../data/demoMessages";

const prompts = [
  { id: "default", label: "Show me the safest route to NODE-18" },
  { id: "sos", label: "Find nearby SOS alerts" },
  { id: "disconnected", label: "Show disconnected zones" },
  { id: "relays", label: "Which nodes are best relays?" },
  { id: "weakest", label: "Where is the network weakest?" },
  { id: "alternate", label: "Show alternate route" },
] as const;

type PromptId = (typeof prompts)[number]["id"];

export default function RelayAssistant() {
  const [active, setActive] = useState<PromptId>("default");
  const [input, setInput] = useState("What is happening in the network?");
  const result = assistantRoutes[active];

  return (
    <section className="section-paper py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <div className="grid gap-16 lg:grid-cols-2">
          <div>
            <p className="label-meta label-meta-dark mb-4">Relay Intelligence</p>
            <h2 className="font-display text-[clamp(2rem,5vw,3.5rem)] leading-tight text-ink">
              Understand the network at a glance.
            </h2>
            <p className="mt-6 max-w-md font-sans text-sm leading-relaxed text-olive-brown">
              An intelligent network assistant that helps operators interpret topology,
              routes, and node health — not a generic chatbot.
            </p>

            <div className="mt-10 flex flex-wrap gap-2">
              {prompts.map((p) => (
                <button
                  key={p.id}
                  type="button"
                  onClick={() => {
                    setActive(p.id);
                    setInput(p.label);
                  }}
                  className={`border px-3 py-2 font-sans text-[0.6rem] tracking-[0.1em] uppercase transition-colors cursor-pointer ${
                    active === p.id
                      ? "border-ink bg-ink text-paper"
                      : "border-olive-brown/30 text-olive-brown hover:border-ink"
                  }`}
                >
                  {p.label}
                </button>
              ))}
            </div>
          </div>

          <div className="border border-olive-brown/20 bg-ink p-6 md:p-8">
            <div className="mb-6 border-b border-stone/10 pb-4">
              <p className="label-meta text-stone/50">{input}</p>
            </div>

            <AnimatePresence mode="wait">
              <motion.div
                key={active}
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -12 }}
                transition={{ duration: 0.3 }}
              >
                {result.reliability > 0 ? (
                  <>
                    <p className="label-meta mb-4 text-gold">Best Available Route</p>
                    <div className="space-y-2">
                      {result.route.map((node, i) => (
                        <div key={`${node}-${i}`} className="flex items-center gap-3">
                          <span className="font-sans text-sm text-paper">{node}</span>
                          {i < result.route.length - 1 && (
                            <span className="text-stone/40">↓</span>
                          )}
                        </div>
                      ))}
                    </div>
                    <div className="mt-8 grid grid-cols-2 gap-4 border-t border-stone/10 pt-6">
                      <div>
                        <p className="label-meta text-stone/50">Hops</p>
                        <p className="font-display text-2xl text-paper">{result.hops}</p>
                      </div>
                      <div>
                        <p className="label-meta text-stone/50">Reliability</p>
                        <p className="font-display text-2xl text-gold">{result.reliability}%</p>
                      </div>
                      <div>
                        <p className="label-meta text-stone/50">Battery</p>
                        <p className="font-sans text-sm text-paper capitalize">{result.battery}</p>
                      </div>
                      <div>
                        <p className="label-meta text-stone/50">Stability</p>
                        <p className="font-sans text-sm text-paper capitalize">{result.stability}</p>
                      </div>
                    </div>
                  </>
                ) : (
                  <div>
                    <p className="label-meta mb-4 text-sos">Disconnected Zone</p>
                    <p className="font-sans text-sm text-stone/70">
                      {result.route.join(" · ")} — no active route available.
                    </p>
                  </div>
                )}
              </motion.div>
            </AnimatePresence>

            <p className="label-meta mt-6 text-stone/25">Simulation · Demo Data</p>
          </div>
        </div>
      </div>
    </section>
  );
}
