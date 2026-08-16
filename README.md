# RELAY — Decentralized Disaster Mesh Network

**When infrastructure disappears, people become the network.**

RELAY is a research prototype for decentralized disaster communication. Nearby smartphones discover each other and relay messages across multi-hop paths when cellular networks and internet connectivity fail.

This repository combines three components into one working system:

| Component | Path | Description |
|-----------|------|-------------|
| **Web** | `src/` | Premium landing page + live mesh simulation UI |
| **Gateway** | `gateway/` | Node.js API server with mesh simulator + WebSocket |
| **Android** | `app/` | Kotlin BLE mesh prototype (real device networking) |

> **Research prototype** — not for production emergency use. See [docs/LIMITATIONS.md](docs/LIMITATIONS.md).

---

## Quick Start

### Prerequisites

- **Node.js** ≥ 22.12
- **Android Studio** + JDK 17 (for the Android app only)

### Run the full web app (landing + backend)

```bash
# Install dependencies
npm run install:all

# Start gateway API (port 4000) + web UI (port 3000)
npm run dev
```

Open **http://localhost:3000**

The landing page connects to the gateway automatically. Interactive demos (network status, failure simulation, SOS routing) use live API data when the gateway is running, and fall back to local demo data when offline.

### Run components separately

```bash
# Gateway API only
npm run dev:gateway        # http://localhost:4000

# Web UI only
npm run dev:web            # http://localhost:3000
```

### Production build

```bash
npm run build
npm run preview            # serves built site + gateway
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Browser (localhost:3000)                   │
│  Astro + React landing page · interactive mesh visualizations │
└──────────────────────────┬──────────────────────────────────┘
                           │ REST /api/*  +  WebSocket /ws
┌──────────────────────────▼──────────────────────────────────┐
│                  Gateway Server (port 4000)                    │
│  MeshSimulator · route calculation · failure/SOS simulation   │
└──────────────────────────┬──────────────────────────────────┘
                           │ (future: SOS upload, fleet sync)
┌──────────────────────────▼──────────────────────────────────┐
│              Android App (BLE mesh on real devices)            │
│  MeshNetworkEngine · BLE transport · Room DB · Compose UI     │
└─────────────────────────────────────────────────────────────┘
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the Android mesh engine design.

---

## Gateway API

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/health` | GET | Health check |
| `/api/network/status` | GET | Live mesh status |
| `/api/nodes` | GET | All nodes + edges |
| `/api/messages` | GET | Message queue |
| `/api/snapshot` | GET | Full state snapshot |
| `/api/centerpiece` | GET | Failure demo topology |
| `/api/routes/:targetId` | GET | Route to a node |
| `/api/simulate/disable` | POST | `{ nodeId, mode }` — disable a node |
| `/api/simulate/sos` | POST | Send SOS through mesh |
| `/api/simulate/reset` | POST | Reset simulation |
| `/ws` | WebSocket | Live state updates |

Example:

```bash
# Disable node C and watch route recovery
curl -X POST http://localhost:4000/api/simulate/disable \
  -H 'Content-Type: application/json' \
  -d '{"nodeId":"C","mode":"centerpiece"}'

# Send SOS
curl -X POST http://localhost:4000/api/simulate/sos
```

---

## Android App

The Android prototype implements real BLE-based mesh networking on physical devices.

### Build

```bash
./gradlew assembleDebug
./gradlew test
```

### Requirements

- Android SDK 34
- JDK 17
- Multiple Android devices with Bluetooth for multi-device demo

### Screens

- **Home** — mesh status, SOS
- **Messages** — send/receive with delivery states
- **Mesh** — live topology graph
- **Map** — offline relative map
- **Simulation** — virtual nodes + failure controls
- **Logs** — protocol event log

See [docs/DEMO.md](docs/DEMO.md) for the 5-phone demo script.

---

## Protocol

DMESH uses application-layer routing over BLE GATT links:

```
Phone → BLE Discovery → Peer Link → Routing (RREQ/RREP/RERR) → Store & Forward → SOS/Messages
```

Packet types: `BEACON`, `RREQ`, `RREP`, `RERR`, `DATA`, `SOS`, `ACK`

Full spec: [docs/PROTOCOL.md](docs/PROTOCOL.md)

---

## Project Structure

```
├── src/                  # Astro web app
│   ├── components/       # Landing page sections
│   ├── context/          # MeshProvider (API connection)
│   ├── hooks/            # useMeshApi, useNetworkAnimation
│   ├── lib/              # API client
│   └── data/             # Demo fallback data
├── gateway/              # Node.js API + mesh simulator
│   └── src/mesh/         # Routing engine, MeshSimulator
├── app/                  # Android Kotlin mesh prototype
├── docs/                 # Architecture, protocol, demo, limitations
└── package.json          # Root scripts (dev, build, preview)
```

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `4000` | Gateway server port |
| `PUBLIC_API_URL` | `""` (proxy) | API base URL for web (empty = use dev proxy) |

Copy `.env.example` to `.env` if deploying separately:

```bash
cp .env.example .env
```

---

## What Works Today

- ✅ Premium editorial landing page with cinematic design
- ✅ Live gateway API with mesh simulation
- ✅ WebSocket real-time state updates
- ✅ Interactive failure simulation (disable node → route recovery → SOS)
- ✅ Android BLE mesh prototype with full protocol stack
- ✅ Shared protocol documentation

## Roadmap

- [ ] Android app reports state to gateway when internet available
- [ ] Gateway SOS ingestion for responder dashboards
- [ ] Shared TypeScript/Kotlin protocol package
- [ ] Browser WebRTC mesh for lab demos

---

## License

Research prototype for demonstration purposes.

## Disclaimer

This system is designed for infrastructure-disrupted environments as a **research concept**. It does not claim to work in every disaster scenario. Simulated metrics are labeled as demo data. Do not rely on this prototype for real emergencies.
