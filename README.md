# 🔨 OmniRepair

> **Repair Vanilla & RPG Items Safely. MMOItems Supported.**

<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.1-blue?style=flat-square" alt="Version">
  <img src="https://img.shields.io/badge/java-21+-orange?style=flat-square" alt="Java">
  <img src="https://img.shields.io/badge/platform-Paper%20%7C%20Spigot%20%7C%20Purpur-green?style=flat-square" alt="Platform">
  <img src="https://img.shields.io/badge/minecraft-26.2-brightgreen?style=flat-square" alt="Minecraft">
  <img src="https://img.shields.io/badge/license-MIT-lightgrey?style=flat-square" alt="License">
</p>

---

## 📋 Overview

OmniRepair is a lightweight, performance-focused Minecraft repair plugin that supports both vanilla items and MMOItems custom equipment. Built with clean architecture and production-ready code.

### ✨ Key Features

- **Universal Durability Detection** — Vanilla tools/armor/weapons + MMOItems custom durability
- **Simplified Repair GUI** — 27-slot interface with 3 buttons (Repair Hand, Repair All, Close)
- **Multiple Payment Methods** — Vault economy, XP levels, item costs, or free repair
- **NBT Preservation** — Safe cloning with full enchantment/lore/custom data protection
- **Blacklist System** — 4-tier filtering: materials, lore, names, MMOItems IDs
- **Anvil Protection** — Prevents MMOItems from being enchanted in vanilla anvils
- **Soft Dependencies** — Graceful degradation without required plugins

---

## 📥 Installation

### Requirements

- **Java 25+**
- **Paper/Spigot/Purpur 1.21+**
- **Optional:** MMOItems 6.9+, Vault 1.7+, WorldGuard 7.0+

### Quick Start

1. Download the latest JAR from [Releases](https://github.com/Syaaddd/OmniRepair/releases)
2. Place in your server's `plugins/` folder
3. Start/restart the server
4. Configure in `plugins/OmniRepair/config.yml`

### Build from Source

```bash
# Using Maven wrapper (recommended)
mvnw.cmd clean package

# Or with installed Maven
mvn clean package
```

Output: `target/OmniRepair-1.0.1.jar`

---

## 🎮 Usage

### Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/repair` | Open repair GUI for yourself | `omnirepair.use` |
| `/repair <player>` | Open repair GUI for another player | `omnirepair.admin` |
| `/repair reload` | Reload configuration | `omnirepair.admin` |
| `/repair debug` | Toggle debug mode | `omnirepair.admin` |

### GUI Layout

```
┌─────────────────────────────────────────────────────────┐
│                   🔨 RPG Mender                         │
├─────────────────────────────────────────────────────────┤
│  [    ] [    ] [    ] [    ] [Close] [    ] [    ] ... │
│  [    ] [Hand] [    ] [ All ] [    ] [    ] [    ] ... │
│  [    ] [    ] [    ] [    ] [    ] [    ] [    ] ... │
└─────────────────────────────────────────────────────────┘
```

- **Slot 10 (Repair Hand)** — Repair item in your hand
- **Slot 12 (Repair All)** — Repair all damaged items in inventory
- **Slot 4 (Close)** — Close the GUI

---

## ⚙️ Configuration

### Main Settings (`config.yml`)

```yaml
settings:
  debug: false
  support-mmoitems: true
  use-economy: true
  cost-per-percent: 10.0
  max-cost: 5000.0
  min-cost: 5.0
  max-bulk-repair: 360
```

### Economy Options

| Method | Config | Description |
|--------|--------|-------------|
| **Vault Economy** | `economy.enabled: true` | Withdraw from player balance |
| **XP Levels** | `economy.use-xp-cost: true` | Deduct XP levels |
| **Item Cost** | `economy.use-item-cost: true` | Consume specific items |
| **Free Repair** | `omnirepair.free` permission | Bypass all costs |

### Blacklist System

```yaml
blacklist:
  materials:
    - BEDROCK
    - BARRIER
  lore-contains:
    - "&lEVENT ITEM"
    - "Unrepairable"
  name-contains:
    - "Event Item"
  mmoitems-ids:
    - "EXAMPLE_SWORD_EVENT"
```

### MMOItems Integration

```yaml
mmoitems:
  enabled: true
  custom-cost-multiplier: 1.5
  sync-lore: true
  lore-format:
    enabled: true
    line: "&7Durability: &a{current} &7/ &a{max} &7(&e{percent}%&7)"
    position: "ADD_BOTTOM"
```

---

## 🛡️ Safety & Protection

| Protection | Implementation |
|------------|----------------|
| **NBT Preservation** | Clone + verify: enchantments, display name, lore, custom model data, PDC keys |
| **Soulbound Check** | Scan lore for soulbound keywords from config |
| **Anvil Protection** | Block MMOItems from anvil enchant (prevents EXP leak) |
| **Blacklist System** | 4-tier: material, lore-contains, name-contains, MMOItems IDs |
| **Cost Cap** | `max-cost` and `min-cost` prevent exploitation |
| **Rollback Safety** | Clone item BEFORE repair; if fails, original unchanged |

---

## 🔌 Plugin Integrations

| Plugin | Status | Method | Fallback |
|--------|--------|--------|----------|
| **MMOItems** `6.9+` | ✅ Soft Dependency | Reflection API + NBT PDC | NBT-only |
| **Vault** `1.7+` | ✅ Soft Dependency | ServicesManager | Free repair |
| **AdvancedEnchantments** | ✅ Soft Dependency | Reflection API + NBT PDC | NBT-only |
| **WorldGuard** `7.0+` | ⚙️ Config Only | Region checks | N/A |

---

## 🏗️ Architecture

OmniRepair uses **layered architecture** with clean separation of concerns:

- **Strategy Pattern** for repair algorithms
- **Facade Pattern** for centralized access
- **Soft Dependencies** for graceful degradation

---

## 📝 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `omnirepair.use` | `true` | Use repair GUI |
| `omnirepair.hand` | `true` | Use /repair hand |
| `omnirepair.bulk` | `false` | Use /repair all |
| `omnirepair.admin` | `op` | Admin commands (reload, debug) |
| `omnirepair.free` | `false` | Free repair (bypass costs) |

---

## 🐛 Troubleshooting

### Plugin doesn't load
- Ensure Java 21+ is installed
- Check server version (1.21+)
- Verify all dependencies are installed

### MMOItems repair not working
- Check `mmoitems.enabled: true` in config
- Verify MMOItems version (6.9+)
- Enable debug mode: `/repair debug`

### Economy not deducting
- Verify Vault is installed
- Check economy plugin is compatible
- Ensure `economy.enabled: true`

### GUI not opening
- Check permission: `omnirepair.use`
- Verify no conflicting plugins
- Check console for errors

---

## 🗺️ Roadmap

- [ ] Multi-language support (i18n)
- [ ] Repair animations and particle effects
- [ ] Region-specific repair costs
- [ ] Repair preview before confirmation
- [ ] Statistics tracking (repairs, economy)
- [ ] Web API for remote management

---

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details.

---

## 🔗 Links

- **Source Code:** https://github.com/Syaaddd/OmniRepair
- **Issues:** https://github.com/Syaaddd/OmniRepair/issues
- **Releases:** https://github.com/Syaaddd/OmniRepair/releases

---

**Made with ❤️ for the Minecraft community**
