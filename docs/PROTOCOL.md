# DMesh Protocol (Prototype v1)

## Node Identity

- ID format: `NODE-{6 hex chars}` (cryptographically random on first launch)
- Display name: user-configurable
- Key pair: RSA 2048 in Android Keystore

## Beacon (Discovery)

```json
{
  "protocol": "DMESH",
  "version": 1,
  "nodeId": "NODE-183A",
  "battery": 72,
  "role": "RELAY",
  "displayName": "Mesh Node"
}
```

## Packet Types

| Type | Purpose |
|------|---------|
| BEACON | Discovery metadata |
| RREQ | Route request |
| RREP | Route reply |
| RERR | Route error |
| DATA | Application message |
| SOS | Emergency broadcast |
| ACK | Delivery acknowledgment |

## Message Envelope

```json
{
  "messageId": "uuid",
  "sourceId": "NODE-A",
  "destinationId": "NODE-D",
  "timestamp": "...",
  "ttl": 12,
  "priority": "NORMAL",
  "type": "TEXT",
  "payload": "...",
  "signature": "...",
  "route": []
}
```

## Routing (Simplified AODV)

1. Source sends RREQ with `requestId`, `hopCount`, `destId`
2. Intermediates record reverse route and forward (dedupe by `requestId`)
3. Destination replies with RREP
4. Route stored with sequence number + expiration
5. RERR triggers alternate route discovery

## Route Scoring

```
routeScore = 10 * hops + 0.5 * (100 - avgRssi) + 0.3 * (100 - minBattery) + 0.1 * ageSeconds
```

Lower score wins.

## Flood Protection

- TTL decremented each hop (default 12)
- `SeenMessageCache` prevents duplicate forwarding
- Priority queue: CRITICAL > HIGH > NORMAL > LOW
- Max forwarding queue size: 200

## Store-and-Forward

Messages without a route enter `STORED` state. When neighbors/routes appear, engine retries delivery.

## SOS

- Priority: CRITICAL
- Includes GPS coordinates, battery, optional text
- Forwarded even under conservative battery relay policy

## Security (Prototype Limitations)

- Messages signed with device-local key
- Replay protection via messageId cache + TTL
- Full cross-node PKI not implemented in v1
