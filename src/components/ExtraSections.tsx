import { motion } from "motion/react";
import { meshHeroNodes, meshHeroEdges } from "../data/demoNetwork";

export default function OneNodeSection() {
  const nodes = meshHeroNodes;
  const edges = meshHeroEdges;

  const getPos = (id: string) => nodes.find((n) => n.id === id);

  return (
    <section className="section-paper py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <div className="grid gap-16 lg:grid-cols-2 lg:items-center">
          <div className="relative">
            <svg viewBox="0 0 600 380" className="w-full" aria-hidden="true">
              {edges.map(([a, b]) => {
                const na = getPos(a);
                const nb = getPos(b);
                if (!na || !nb) return null;
                return (
                  <line
                    key={`${a}-${b}`}
                    x1={na.x}
                    y1={na.y}
                    x2={nb.x}
                    y2={nb.y}
                    stroke="#5B4F32"
                    strokeWidth="1"
                    opacity="0.4"
                  />
                );
              })}
              {nodes.map((n) => (
                <circle key={n.id} cx={n.x} cy={n.y} r="6" fill="#768965" />
              ))}
            </svg>
          </div>

          <div>
            <h2 className="font-display text-[clamp(2rem,5vw,4rem)] leading-tight text-ink">
              One node.
              <br />
              Infinite routes.
            </h2>
            <p className="mt-6 max-w-md font-sans text-sm leading-relaxed text-olive-brown">
              When a direct connection does not exist, RELAY uses surrounding devices as
              temporary relays to construct alternate paths.
            </p>
            <div className="mt-8 space-y-2">
              <p className="label-meta label-meta-dark">Route Example</p>
              <p className="font-sans text-sm text-ink">04 → 09 → 15 → 18</p>
              <p className="font-sans text-xs text-olive-brown">4 hops · 94% reliability · Demo</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export function BatterySection() {
  const nodes = [
    { id: "NODE A", battery: 91 },
    { id: "NODE B", battery: 72 },
    { id: "NODE C", battery: 14 },
    { id: "NODE D", battery: 83 },
  ];

  return (
    <section className="section-ink py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <h2 className="font-display text-[clamp(2rem,5vw,3.5rem)] leading-tight text-paper">
          Survival is also
          <br />
          <span className="text-stone">an energy problem.</span>
        </h2>

        <div className="mt-12 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {nodes.map((n) => (
            <div key={n.id} className="border border-stone/10 p-6">
              <p className="label-meta text-stone/50">{n.id}</p>
              <p className="font-display mt-2 text-3xl text-paper">{n.battery}%</p>
              <div className="mt-3 h-1 w-full bg-stone/10">
                <div
                  className="h-full bg-gold transition-all"
                  style={{ width: `${n.battery}%` }}
                />
              </div>
            </div>
          ))}
        </div>

        <p className="mt-10 font-display text-xl text-gold">
          High reliability + Low energy cost = Better route
        </p>
        <p className="mt-2 font-sans text-sm text-stone/60">
          Relay selection can account for node health and battery levels.
        </p>
      </div>
    </section>
  );
}

const timeline = [
  { time: "02:14 AM", text: "The towers go dark." },
  { time: "02:16", text: "Phones begin discovering one another." },
  { time: "02:17", text: "The first mesh forms." },
  { time: "02:19", text: "An SOS reaches a rescue team 6 hops away." },
  { time: "02:23", text: "A disconnected zone rejoins the network." },
];

export function DisasterScenario() {
  const bgImage =
    "https://images.unsplash.com/photo-1519451241324-20b4ea2c8dfd?w=1920&q=80&auto=format&fit=crop";

  return (
    <section className="relative overflow-hidden">
      <div className="relative min-h-[60vh]">
        <img
          src={bgImage}
          alt="City skyline at night with darkness spreading"
          className="image-warm h-full w-full object-cover min-h-[60vh]"
          loading="lazy"
        />
        <div className="absolute inset-0 bg-ink/60" />
        <div className="absolute inset-0 flex items-center px-6 md:px-10 lg:px-14">
          <div className="mx-auto w-full max-w-[1440px]">
            <p className="font-display text-[clamp(3rem,8vw,7rem)] text-paper">02:14 AM</p>
            <p className="font-display mt-4 text-3xl text-stone md:text-4xl">
              The towers go dark.
            </p>
          </div>
        </div>
      </div>

      <div className="section-ink py-16 md:py-24">
        <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
          {timeline.slice(1).map((item, i) => (
            <motion.div
              key={item.time}
              className="grid gap-4 border-b border-stone/10 py-10 md:grid-cols-12"
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-50px" }}
              transition={{ delay: i * 0.1 }}
            >
              <p className="font-display text-2xl text-gold md:col-span-3">{item.time}</p>
              <p className="font-sans text-sm leading-relaxed text-stone/80 md:col-span-9 md:text-base">
                {item.text}
              </p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
}

export function HumanCentered() {
  const stories = [
    "A family searching for each other.",
    "A rescue worker coordinating a team.",
    "A volunteer carrying a message through a disconnected zone.",
  ];
  const bgImage =
    "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=1200&q=80&auto=format&fit=crop";

  return (
    <section className="section-paper py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <div className="grid gap-12 lg:grid-cols-2 lg:items-center">
          <div className="overflow-hidden">
            <img
              src={bgImage}
              alt="Person holding a smartphone in low light"
              className="image-warm aspect-[4/5] w-full object-cover"
              loading="lazy"
            />
          </div>
          <div>
            <h2 className="font-display text-[clamp(2rem,5vw,3.5rem)] leading-tight text-ink">
              Behind every node
              <br />
              is a person.
            </h2>
            <p className="mt-6 font-sans text-sm leading-relaxed text-olive-brown">
              Every connection represents someone trying to reach someone else.
            </p>
            <ul className="mt-10 space-y-6">
              {stories.map((s) => (
                <li key={s} className="border-l-2 border-gold/40 pl-4 font-sans text-sm text-ink">
                  {s}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
}

export function ProtocolDemo() {
  return (
    <section className="section-ink py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <p className="label-meta mb-4">Protocol</p>
        <h2 className="font-display text-[clamp(2rem,5vw,3rem)] text-paper">
          Route recovery in action
        </h2>
        <div className="mt-12 grid gap-8 md:grid-cols-3">
          <div className="border border-stone/10 p-6">
            <p className="label-meta text-stone/50">Route Request</p>
            <div className="mt-4 space-y-2 font-sans text-sm text-paper">
              {["NODE-A", "NODE-B", "NODE-C", "NODE-D"].map((n, i) => (
                <div key={n}>
                  {n}
                  {i < 3 && <span className="ml-2 text-stone/40">↓</span>}
                </div>
              ))}
            </div>
          </div>
          <div className="flex flex-col items-center justify-center border border-sos/30 p-6">
            <p className="label-meta text-sos">Route Error</p>
            <p className="mt-4 text-stone/40">↓</p>
            <p className="label-meta text-gold">Discovery</p>
            <p className="mt-4 text-stone/40">↓</p>
            <p className="label-meta text-sage">Alternative Route</p>
          </div>
          <div className="border border-gold/30 p-6">
            <p className="label-meta text-gold">Recovered</p>
            <div className="mt-4 space-y-2 font-sans text-sm text-paper">
              {["NODE-A", "NODE-E", "NODE-D"].map((n, i) => (
                <div key={n}>
                  {n}
                  {i < 2 && <span className="ml-2 text-gold">↓</span>}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
