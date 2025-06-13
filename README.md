# PortMapper

**PortMapper** is a simple Kotlin desktop application built with Jetpack Compose that allows you to open and close ports on your router using UPnP. It supports both **TCP** and **UDP** protocols.

## Features

- Input one or multiple ports (comma-separated)
- Open and close ports via UPnP (TCP and UDP)
- Manage currently open ports in a separate window

## Requirements

- Java 8 or newer (Only for building from sources)
- A router with UPnP support enabled

## Based on

This project uses [WaifUPnP](https://github.com/adolfintel/WaifUPnP) — a minimalistic Java UPnP library.

## How to Run

### Option 1: Download from Releases

1. Go to the [Releases](https://github.com/AxieFeat/KPortMapper/releases) page.
2. Download the latest executable file for your OS.
3. Run it and install port mapper.
### Option 2: Build from Source

1. Clone this repository:
   ```bash
   git clone https://github.com/AxieFeat/KPortMapper.git
   ```
2. Open the project in IntelliJ IDEA or another Kotlin-compatible IDE.
3. Run main() from `xyz.axie.portmapper.MainKt`.