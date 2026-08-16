export default function CTA() {
  return (
    <section className="section-paper py-32 md:py-48">
      <div className="mx-auto max-w-[1440px] px-6 text-center md:px-10 lg:px-14">
        <h2 className="font-display text-[clamp(2.5rem,7vw,6rem)] leading-[1.05] text-ink">
          Build the network
          <br />
          before you need it.
        </h2>
        <p className="mx-auto mt-8 max-w-lg font-sans text-sm leading-relaxed text-olive-brown">
          RELAY is an experimental decentralized communication platform designed for
          environments where conventional infrastructure cannot be trusted.
        </p>
        <div className="mt-12 flex flex-wrap justify-center gap-4">
          <a href="#demo" className="btn-primary btn-ink">
            Explore the Prototype <span className="arrow">→</span>
          </a>
          <a href="#technology" className="btn-secondary btn-ink">
            View Architecture
          </a>
        </div>
      </div>
    </section>
  );
}
