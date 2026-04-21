# HNS/ENS Android Resolver

A lightweight Android VPN-based DNS resolver for the HNS and ENS ecosystem.

It routes DNS queries through custom resolvers, allowing you to access HNS/ENS domains directly from your device.

---

## Features

- Works system-wide (apps & browsers)
- Default resolver:  
  -> https://github.com/james-stevens/handshake-volume-resolver
- Support for custom DNS resolvers
- Ability to fetch records (A, AAAA, NS, CNAME, MX, TXT, TLSA)

---

## Usage

1. Install and open the app  
2. Activate HNS
3. Open your browser and access HNS domains  

 Important:  
You must type domains like:

http://example/ or example/

This ensures the browser treats it as a valid URL and sends the request correctly.

---

## Configuration

- You can set your own HNS resolvers in the app
- Supports switching between resolvers
- Can fetch records depending on the configured resolver

---

## How it works

The app uses Android's `VpnService` to intercept DNS traffic and redirect it to HNS/ENS-compatible resolvers.

---

## Credits

This app uses the resolver developed by James Stevens by default.

- Source: https://github.com/james-stevens/handshake-volume-resolver

This project is based on the original **Daedalus** DNS resolver:

- Source: https://github.com/iTXTech/Daedalus

Special thanks to the original authors for their work.

---

## Notes

- Requires VPN permission to function
- Does not route full traffic — DNS only
- Some browsers may cache DNS aggressively

---

## License

Check original repository for details.
