import Navbar from "./Navbar";
import Hero from "./Hero";
import EditorialStatement from "./EditorialStatement";
import MeshGraph from "./MeshGraph";
import RelayAssistant from "./RelayAssistant";
import OneNodeSection, {
  BatterySection,
  DisasterScenario,
  HumanCentered,
  ProtocolDemo,
} from "./ExtraSections";
import NetworkGrid from "./NetworkGrid";
import NodeDetail from "./NodeDetail";
import MessageDetail from "./MessageDetail";
import StoreForwardStory from "./StoreForwardStory";
import FailureSimulation from "./FailureSimulation";
import OfflineMap from "./OfflineMap";
import Architecture from "./Architecture";
import DeviceShowcase, { MobilePhoneSection } from "./DeviceShowcase";
import FieldTest from "./FieldTest";
import CTA from "./CTA";
import Footer from "./Footer";
import { MeshProvider } from "../context/MeshContext";

export default function RelayLanding() {
  return (
    <MeshProvider>
      <a href="#main" className="skip-link">
        Skip to content
      </a>
      <Navbar />
      <main id="main">
        <Hero />
        <EditorialStatement
          variant="paper"
          lines={[
            "Communication should not disappear",
            "when infrastructure does.",
          ]}
          meta="The Problem"
        />
        <section className="section-ink py-24 md:py-32">
          <div className="mx-auto max-w-[1440px] px-6 md:px-10 lg:px-14">
            <p className="mx-auto max-w-2xl text-center font-sans text-base leading-[1.9] text-stone/70 md:text-lg md:leading-[2]">
              RELAY enables nearby smartphones to discover one another and exchange messages
              directly, creating temporary multi-hop communication paths when conventional
              networks are unavailable.
            </p>
          </div>
        </section>
        <EditorialStatement
          variant="ink"
          lines={["Every phone", "can become", "a relay."]}
          sublines={["One device", "One connection", "One possible path"]}
          meta="The Concept"
          showLines
        />
        <MeshGraph />
        <OneNodeSection />
        <RelayAssistant />
        <FailureSimulation />
        <NetworkGrid />
        <NodeDetail />
        <MessageDetail />
        <DeviceShowcase />
        <MobilePhoneSection />
        <StoreForwardStory />
        <BatterySection />
        <OfflineMap />
        <DisasterScenario />
        <HumanCentered />
        <Architecture />
        <ProtocolDemo />
        <FieldTest />
        <CTA />
      </main>
      <Footer />
    </MeshProvider>
  );
}
