# Known Limitations

## Radio & Range

- BLE effective range: ~10–50m per hop (environment dependent)
- Not line-of-sight through thick walls reliably
- Interference from other BLE/Wi-Fi devices

## Android Constraints

- ~7 concurrent BLE GATT connections per device (OEM varies)
- BLE scanning requires location permission (Android 10+)
- Background operation limited; foreground service required
- Battery consumption increases with active scanning/relaying

## Scalability

- Prototype designed for small clusters (handful of physical devices)
- Large networks (100+) via simulation only
- No native Bluetooth Mesh or Wi-Fi mesh standard compliance

## Security

- No full PKI or E2E encryption in v1
- Cross-node signature verification limited
- Do not use for real emergencies

## Routing

- Simplified AODV; not full AODV/OLSR production implementation
- Route scoring is heuristic, not QoS-guaranteed
- Partition healing depends on mobility or new nodes bridging groups

## Map

- Custom offline relative map only (no Google Maps)
- Location accuracy depends on GPS availability

## Gateway

- Optional Internet upload is stub only
- Mesh does not depend on gateway

## What We Do NOT Claim

- "Bluetooth mesh" in the sense of Bluetooth SIG Mesh Profile
- Infrastructure-free unlimited range
- Production-grade disaster response system
