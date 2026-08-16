# Architecture

## Layered Design

```
ui/                 Jetpack Compose screens + ViewModels
MeshController      Application orchestration
MeshNetworkEngine   Protocol + routing + forwarding (single source of truth)
transport/          BleTransport, SimulatedTransport
security/           Node identity + signing (Android Keystore)
database/           Room persistence
simulation/         Virtual nodes (same engine)
gateway/            Optional Internet gateway stub
```

## Data Flow

1. `BleTransport` receives bytes → `MeshNetworkEngine.onPacketReceived`
2. Engine validates TTL, deduplicates, routes protocol packets
3. `RoutingEngine` handles RREQ/RREP/RERR
4. `StoreForwardManager` queues when no route exists
5. UI observes `Flow` state from engine tables

## Module Map

| Package | Responsibility |
|---------|----------------|
| `mesh.protocol` | Packet types, serialization |
| `mesh.neighbor` | Local neighbor table |
| `mesh.routing` | AODV-inspired routing + route scoring |
| `mesh.forwarding` | Priority queue, seen cache |
| `mesh.storeforward` | Pending messages |
| `transport.ble` | Dual-role BLE GATT |
| `simulation` | SimulatedTransport + virtual topology |

## Diagram

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────┐
│  Compose UI │────▶│ MeshController   │────▶│ MeshEngine  │
└─────────────┘     └──────────────────┘     └──────┬──────┘
                                                    │
                     ┌──────────────────────────────┼──────────────────────────────┐
                     ▼                              ▼                              ▼
              BleTransport                   SimulatedTransport                  Room DB
```
