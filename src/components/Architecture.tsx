const layers = [
  "DEVICE",
  "Bluetooth / Wi-Fi",
  "Peer Discovery",
  "Neighbor Table",
  "Routing",
  "Message Queue",
  "Store & Forward",
  "SOS / Messaging / Location",
];

export default function Architecture() {
  return (
    <section id="technology" className="section-paper py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <p className="label-meta label-meta-dark mb-4">Architecture</p>
        <h2 className="font-display text-[clamp(2rem,5vw,4rem)] leading-tight text-ink">
          Under the surface.
        </h2>

        <div className="mt-16 flex flex-col items-center">
          {layers.map((layer, i) => (
            <div key={layer} className="flex w-full max-w-md flex-col items-center">
              <div
                className={`w-full border px-6 py-4 text-center ${
                  i === 0
                    ? "border-ink bg-ink text-paper"
                    : "border-olive-brown/25 bg-paper text-ink"
                }`}
              >
                <span className="font-sans text-[0.65rem] tracking-[0.15em] uppercase">
                  {layer}
                </span>
              </div>
              {i < layers.length - 1 && (
                <div className="flex h-8 flex-col items-center justify-center">
                  <div className="h-full w-px bg-olive-brown/30" />
                  <span className="text-olive-brown/40 text-xs">↓</span>
                </div>
              )}
            </div>
          ))}
        </div>

        <p className="label-meta label-meta-dark mt-12 text-center">
          Technical Journal · Conceptual Architecture
        </p>
      </div>
    </section>
  );
}
