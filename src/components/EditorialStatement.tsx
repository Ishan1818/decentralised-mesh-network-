import { motion } from "motion/react";

interface EditorialStatementProps {
  variant?: "paper" | "ink";
  lines: string[];
  sublines?: string[];
  meta?: string;
  className?: string;
  id?: string;
  showLines?: boolean;
}

export default function EditorialStatement({
  variant = "paper",
  lines,
  sublines,
  meta,
  className = "",
  id,
  showLines = false,
}: EditorialStatementProps) {
  const isPaper = variant === "paper";

  return (
    <section
      id={id}
      className={`relative overflow-hidden ${isPaper ? "section-paper" : "section-ink"} ${className}`}
    >
      {showLines && (
        <svg
          className="pointer-events-none absolute inset-0 h-full w-full opacity-[0.06]"
          aria-hidden="true"
        >
          <line x1="10%" y1="20%" x2="40%" y2="60%" stroke={isPaper ? "#523117" : "#C5BDA4"} strokeWidth="1" />
          <line x1="60%" y1="30%" x2="90%" y2="70%" stroke={isPaper ? "#523117" : "#C5BDA4"} strokeWidth="1" />
          <line x1="30%" y1="80%" x2="70%" y2="40%" stroke={isPaper ? "#523117" : "#C5BDA4"} strokeWidth="1" />
          <circle cx="40%" cy="60%" r="4" fill={isPaper ? "#768965" : "#D7A84A"} />
          <circle cx="60%" cy="30%" r="4" fill={isPaper ? "#768965" : "#D7A84A"} />
          <circle cx="70%" cy="40%" r="4" fill={isPaper ? "#768965" : "#D7A84A"} />
        </svg>
      )}
      <div className="mx-auto max-w-[1440px] px-6 py-24 md:px-10 md:py-32 lg:px-14 lg:py-40">
        {meta && (
          <motion.p
            className={`label-meta mb-8 ${isPaper ? "label-meta-dark" : ""}`}
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
          >
            {meta}
          </motion.p>
        )}

        <motion.div
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: "-80px" }}
          variants={{
            hidden: {},
            visible: { transition: { staggerChildren: 0.12 } },
          }}
        >
          {lines.map((line, i) => (
            <motion.h2
              key={i}
              className={`font-display text-[clamp(2.2rem,6vw,5rem)] leading-[1.05] font-medium ${
                i > 0 ? "mt-1" : ""
              } ${isPaper ? "text-ink" : "text-paper"}`}
              variants={{
                hidden: { opacity: 0, y: 24 },
                visible: { opacity: 1, y: 0 },
              }}
              transition={{ duration: 0.7 }}
            >
              {line}
            </motion.h2>
          ))}
        </motion.div>

        {sublines && (
          <motion.div
            className="mt-10 flex flex-wrap gap-x-8 gap-y-2"
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            transition={{ delay: 0.3 }}
          >
            {sublines.map((line) => (
              <span
                key={line}
                className={`font-sans text-[0.6rem] tracking-[0.25em] uppercase ${
                  isPaper ? "text-olive-brown" : "text-stone"
                }`}
              >
                {line}
              </span>
            ))}
          </motion.div>
        )}
      </div>
    </section>
  );
}
