# HNS Android Resolver

A lightweight Android VPN-based DNS resolver for the Handshake (HNS) ecosystem.

It routes DNS queries through custom resolvers, allowing you to access Handshake domains directly from your device.

---

## Features

- Works system-wide (all apps & browsers)
- Default resolver:  
  -> https://github.com/james-stevens/handshake-volume-resolver
- Support for custom DNS resolvers
- Ability to fetch and resolve Handshake records
- Quick Settings tile to toggle the VPN بسهولة

---

## Usage

1. Install and open the app  
2. Activate the VPN (via app or Quick Settings tile)  
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

The app uses Android's `VpnService` to intercept DNS traffic and redirect it to Handshake-compatible resolvers.

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
