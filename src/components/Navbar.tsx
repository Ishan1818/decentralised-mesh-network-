import { useState, useEffect } from "react";
import { Menu, X } from "lucide-react";

const navLinks = [
  { href: "#network", label: "Network" },
  { href: "#technology", label: "Technology" },
  { href: "#how-it-works", label: "How It Works" },
  { href: "#field-tests", label: "Field Tests" },
  { href: "#about", label: "About" },
];

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 60);
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    document.body.style.overflow = open ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [open]);

  return (
    <>
      <header
        className={`fixed top-0 left-0 right-0 z-50 transition-all duration-500 ${
          scrolled ? "bg-ink/80 backdrop-blur-md" : "bg-transparent"
        }`}
      >
        <nav
          className="mx-auto flex max-w-[1440px] items-center justify-between px-6 py-5 md:px-10 lg:px-14"
          aria-label="Main navigation"
        >
          <a href="#" className="group flex items-center gap-3 no-underline">
            <span className="font-sans text-[0.65rem] font-semibold tracking-[0.35em] text-paper uppercase">
              RELAY
            </span>
            <svg className="hidden h-5 w-5 text-stone/50 sm:block" viewBox="0 0 24 24" aria-hidden="true">
              <circle cx="6" cy="6" r="2" fill="currentColor" />
              <circle cx="18" cy="6" r="2" fill="currentColor" />
              <circle cx="12" cy="18" r="2" fill="currentColor" />
              <line x1="6" y1="6" x2="18" y2="6" stroke="currentColor" strokeWidth="0.75" />
              <line x1="6" y1="6" x2="12" y2="18" stroke="currentColor" strokeWidth="0.75" />
              <line x1="18" y1="6" x2="12" y2="18" stroke="currentColor" strokeWidth="0.75" />
            </svg>
          </a>

          <ul className="hidden items-center gap-8 lg:flex">
            {navLinks.map((link) => (
              <li key={link.href}>
                <a
                  href={link.href}
                  className="font-sans text-[0.6rem] font-medium tracking-[0.18em] text-paper/70 uppercase no-underline transition-colors hover:text-paper"
                >
                  {link.label}
                </a>
              </li>
            ))}
          </ul>

          <div className="hidden items-center gap-6 lg:flex">
            <a href="#demo" className="btn-primary text-[0.6rem]">
              Deploy Demo <span className="arrow">→</span>
            </a>
          </div>

          <button
            type="button"
            className="flex items-center justify-center border border-paper/20 p-2 text-paper lg:hidden"
            onClick={() => setOpen(true)}
            aria-label="Open menu"
          >
            <Menu size={18} strokeWidth={1.5} />
          </button>
        </nav>
      </header>

      {open && (
        <div className="fixed inset-0 z-[60] flex flex-col bg-ink" role="dialog" aria-modal="true">
          <div className="flex items-center justify-between px-6 py-5">
            <span className="font-sans text-[0.65rem] font-semibold tracking-[0.35em] text-paper uppercase">
              RELAY
            </span>
            <button
              type="button"
              onClick={() => setOpen(false)}
              className="border border-paper/20 p-2 text-paper"
              aria-label="Close menu"
            >
              <X size={18} strokeWidth={1.5} />
            </button>
          </div>
          <nav className="flex flex-1 flex-col justify-center gap-8 px-10">
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                onClick={() => setOpen(false)}
                className="font-display text-4xl text-paper no-underline"
              >
                {link.label}
              </a>
            ))}
            <a
              href="#demo"
              onClick={() => setOpen(false)}
              className="btn-primary mt-6 w-fit"
            >
              Deploy Demo <span className="arrow">→</span>
            </a>
          </nav>
        </div>
      )}
    </>
  );
}
