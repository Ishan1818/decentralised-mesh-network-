# Demo Instructions — 5 Physical Android Phones

## Prerequisites

- 5 Android phones (API 26+)
- DMesh Prototype APK installed on each
- Bluetooth enabled on all devices
- Mobile data **OFF**
- Wi-Fi internet **OFF** (device-to-device may remain on for BLE)

## Setup

1. Launch app on all phones; grant Bluetooth, location, and notification permissions.
2. Confirm foreground service shows "DMesh Prototype Active".
3. Place phones within ~10m of each other.
4. Wait 10–30 seconds for discovery.

**Expected:** Each phone shows nearby nodes; total cluster shows ~5 online nodes.

## Demo 1 — Multi-hop Message

1. Label phones A–E mentally by node ID shown on Home screen.
2. On phone A, open **Messages**.
3. Send to phone E's node ID: `"Test message A to E"`.
4. Observe delivery path in UI (e.g. A → B → C → E).

## Demo 2 — Route Recovery

1. With message flow active, move phone C out of range or disable Bluetooth on C.
2. Observe route failure in **Logs**.
3. Send another message A → E.
4. Observe alternate route (e.g. A → D → E).

## Demo 3 — Network Partition (Simulation Mode)

On one phone (or all for consistent demo):

1. Open **Simulation** → Enable simulation.
2. Use **Kill NODE-C** or **Disable Link B-C** to partition groups.
3. Send message A → E → status shows **STORED LOCALLY — WAITING FOR NETWORK**.
4. Tap **Bridge partition C-D** (or add bridging node).
5. Message becomes **DELIVERED** with path shown.

## Demo 4 — SOS

1. On phone A, press **SOS** on Home screen.
2. Other reachable phones show SOS in Messages and Map (red marker).
3. Confirm CRITICAL priority in message list.

## Demo 5 — Infrastructure Independence

On Home screen verify:

```
Internet: OFFLINE
Cellular: UNAVAILABLE
Mesh: ACTIVE
Network still operational without Internet
```

## Simulation-Only Large Network

1. Enable simulation mode.
2. Add virtual nodes for 10–50 node studies.
3. Use failure controls: packet drop, congestion, kill node.
4. Observe **Mesh** graph and **Logs** for routing behavior.
