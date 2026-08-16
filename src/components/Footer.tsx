const footerLinks = [
  { label: "Network", href: "#network" },
  { label: "Technology", href: "#technology" },
  { label: "Field Tests", href: "#field-tests" },
  { label: "Architecture", href: "#technology" },
  { label: "About", href: "#about" },
];

const externalLinks = [
  { label: "GitHub", href: "#" },
  { label: "Documentation", href: "#" },
  { label: "Prototype", href: "#demo" },
];

export default function Footer() {
  return (
    <footer id="about" className="section-ink border-t border-stone/10 py-20 md:py-28">
      <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
        <div className="grid gap-12 md:grid-cols-3">
          <div>
            <p className="font-sans text-[0.65rem] font-semibold tracking-[0.35em] text-paper uppercase">
              RELAY
            </p>
            <p className="mt-4 max-w-xs font-sans text-xs leading-relaxed text-stone/50">
              Communication without towers. Coordination without a central point of failure.
            </p>
          </div>

          <nav aria-label="Footer navigation">
            <ul className="space-y-3">
              {footerLinks.map((link) => (
                <li key={link.label}>
                  <a
                    href={link.href}
                    className="font-sans text-[0.65rem] tracking-[0.12em] text-stone/60 uppercase no-underline transition-colors hover:text-paper"
                  >
                    {link.label}
                  </a>
                </li>
              ))}
            </ul>
          </nav>

          <nav aria-label="External links">
            <ul className="space-y-3">
              {externalLinks.map((link) => (
                <li key={link.label}>
                  <a
                    href={link.href}
                    className="font-sans text-[0.65rem] tracking-[0.12em] text-stone/60 uppercase no-underline transition-colors hover:text-paper"
                  >
                    {link.label}
                  </a>
                </li>
              ))}
            </ul>
          </nav>
        </div>

        <p className="label-meta mt-20 text-stone/30">© 2026 RELAY</p>
      </div>
    </footer>
  );
}
