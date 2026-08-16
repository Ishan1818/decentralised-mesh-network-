# DMesh — Decentralized Disaster Mesh Network Prototype

**RESEARCH PROTOTYPE** — Not for production emergency use.

## Problem

When disasters strike, centralized communication infrastructure often fails: cell towers overload, power fails, routers go offline, and internet backhaul disappears. People with working phones may still be unable to reach each other.

## Solution

DMesh treats each Android phone as a **node** in a dynamic distributed network. Phones discover neighbors over **Bluetooth Low Energy (BLE)**, form local links, and use **application-layer routing** to forward messages across multiple hops. No central server is required for local mesh communication.

```
Phone
  ↓ Discovery (BLE advertising)
  ↓ Transport (BLE GATT dual-role)
  ↓ Routing (AODV-inspired)
  ↓ Forwarding (TTL + priority queue)
  ↓ Store-and-forward
  ↓ Application (messages, SOS, UI)
```

## What This Prototype Actually Implements

| Layer | Mechanism |
|-------|-----------|
| Discovery | BLE advertising + scanning with DMESH service UUID |
| Links | BLE GATT server/client (not native Bluetooth Mesh) |
| Multi-hop | Application routing (RREQ/RREP/RERR) |
| Store-and-forward | Local Room database queue |
| Simulation | Virtual nodes using the **same** mesh engine |

## Architecture

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Protocol

See [docs/PROTOCOL.md](docs/PROTOCOL.md).

## Demo (5 Phones)

See [docs/DEMO.md](docs/DEMO.md).

## Limitations

See [docs/LIMITATIONS.md](docs/LIMITATIONS.md).

## Build

Requirements: Android Studio, SDK 34, JDK 17.

```bash
./gradlew assembleDebug
./gradlew test
```

Install the APK on multiple Android devices, grant Bluetooth and location permissions, disable mobile data and Wi-Fi internet, and run the demo script.

## Screens

- **Home** — mesh status, SOS, prototype banner
- **Messages** — send/receive with delivery states
- **Mesh** — live graph visualization
- **Map** — offline relative map (no Google Maps)
- **Nodes** — neighbor table
- **Logs** — protocol event log
- **Simulation** — virtual nodes + failure controls
- **Settings** — relay mode, TTL, location sharing

## License

Research prototype for demonstration purposes.
