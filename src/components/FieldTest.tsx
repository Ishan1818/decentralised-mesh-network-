export default function FieldTest() {
  const tests = [
    {
      id: "FIELD TEST 01",
      zone: "Urban Zone",
      nodes: 24,
      delivery: "96.8%",
      hops: "3.7",
      latency: "2.4 sec",
    },
    {
      id: "FIELD TEST 02",
      zone: "Mountain Pass",
      nodes: 18,
      delivery: "91.2%",
      hops: "4.1",
      latency: "3.1 sec",
    },
  ];

  return (
    <section id="field-tests" className="section-ink py-24 md:py-32">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <p className="label-meta mb-4">Field Reports</p>
        <h2 className="font-display text-[clamp(2rem,5vw,4rem)] leading-tight text-paper">
          Tested beyond
          <br />
          the laboratory.
        </h2>

        <div className="mt-16 grid gap-6 md:grid-cols-2">
          {tests.map((test) => (
            <article
              key={test.id}
              className="border border-stone/10 p-8 md:p-10"
            >
              <p className="label-meta text-gold">{test.id}</p>
              <p className="mt-2 font-display text-2xl text-paper">{test.zone}</p>

              <div className="mt-8 grid grid-cols-2 gap-6">
                <div>
                  <p className="label-meta text-stone/50">Nodes</p>
                  <p className="font-display text-3xl text-paper">{test.nodes}</p>
                </div>
                <div>
                  <p className="label-meta text-stone/50">Delivery</p>
                  <p className="font-display text-3xl text-paper">{test.delivery}</p>
                </div>
                <div>
                  <p className="label-meta text-stone/50">Avg Hops</p>
                  <p className="font-display text-2xl text-paper">{test.hops}</p>
                </div>
                <div>
                  <p className="label-meta text-stone/50">Median Latency</p>
                  <p className="font-display text-2xl text-paper">{test.latency}</p>
                </div>
              </div>

              <p className="label-meta mt-8 text-stone/40">Simulation · Demo Data</p>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
