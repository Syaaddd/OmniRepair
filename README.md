# 🔨 OmniRepair

**Repair Vanilla & RPG Items Safely. MMOItems Supported.**

[![Version](https://img.shields.io/badge/version-1.0.0--SNAPSHOT-blue)]()
[![Java](https://img.shields.io/badge/java-21-orange)]()
[![Platform](https://img.shields.io/badge/platform-paper%2Fspigot-green)]()
[![License](https://img.shields.io/badge/license-MIT-lightgrey)]()

---

## 📖 Overview

OmniRepair is a comprehensive repair plugin for Minecraft servers that supports both vanilla items and custom RPG items from **MMOItems**. It provides a beautiful GUI, economy integration, and 100% NBT-safe repair operations.

### ✨ Features

- **🎮 Universal Durability Detection**
  - Vanilla items (tools, armor, weapons)
  - MMOItems custom durability (via API)
  - Oraxen & ItemsAdder support (optional)
  - Automatic lore synchronization

- **🖥️ Smart Repair GUI**
  - Interactive RPG-styled interface
  - Live preview of repaired items
  - Cost display before repair
  - Bulk repair option

- **💰 Economy & Cost System**
  - Vault integration (any economy plugin)
  - Configurable cost per durability percent
  - Item cost or XP cost alternatives
  - Free repair permission option

- **🛡️ Safety Protection**
  - 100% NBT preservation guaranteed
  - Soulbound item protection
  - Blacklist support (materials, lore, names)
  - Maximum cost limits
  - Rollback on failure

---

## 📥 Installation

### Requirements

- **Server Software:** Paper, Spigot, or Purpur (1.21+)
- **Java:** Java 21 or higher
- **Optional Dependencies:**
  - MMOItems (for custom RPG item support)
  - Vault + Economy plugin (for repair costs)
  - WorldGuard (for region restrictions)

### Steps

1. Download the latest `OmniRepair.jar` from [Releases](https://github.com/Syaaddd/OmniRepair/releases)
2. Place the JAR file in your server's `plugins/` folder
3. Start or restart your server
4. Configure the plugin in `plugins/OmniRepair/config.yml`

---

## 🎮 Usage

### Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/repair` | Open the repair GUI | `omnirepair.use` |
| `/repair hand` | Repair held item instantly | `omnirepair.hand` |
| `/repair all` | Repair all inventory items | `omnirepair.bulk` |
| `/repair reload` | Reload configuration | `omnirepair.admin` |
| `/repair help` | Show help message | `omnirepair.use` |

### Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `omnirepair.use` | Use the repair GUI | `true` |
| `omnirepair.hand` | Use instant hand repair | `true` |
| `omnirepair.bulk` | Use bulk repair | `false` (OP) |
| `omnirepair.admin` | Admin commands | `op` |
| `omnirepair.free` | Free repairs (bypass cost) | `false` |

---

## ⚙️ Configuration

### Basic Settings (`config.yml`)

```yaml
settings:
  # Use MMOItems API for custom durability
  support-mmoitems: true
  
  # Use Vault Economy for repair costs
  use-economy: true
  
  # Cost per 1% durability lost
  cost-per-percent: 10.0
  
  # Maximum cost for single repair
  max-cost: 5000.0
  
  # Minimum cost (even for small repairs)
  min-cost: 5.0
```

### Blacklist

Prevent specific items from being repaired:

```yaml
blacklist:
  materials:
    - BEDROCK
    - BARRIER
    - ENCHANTED_GOLDEN_APPLE
  
  lore-contains:
    - "&lEVENT ITEM"
    - "Unrepairable"
  
  name-contains:
    - "Event Item"
```

### MMOItems Integration

```yaml
mmoitems:
  enabled: true
  sync-lore: true
  custom-cost-multiplier: 1.5  # 50% more expensive than vanilla
  
  lore-format:
    enabled: true
    line: "&7Durability: &a{current} &7/ &a{max} &7(&e{percent}%&7)"
```

### Economy Options

```yaml
economy:
  enabled: true
  use-economy: true
  
  # Alternative: Use XP instead of money
  use-xp-cost: false
  xp-per-percent: 0.5
  
  # Alternative: Use items instead of money
  use-item-cost: false
  cost-item:
    material: "IRON_INGOT"
    amount-per-percent: 0.01
```

---

## 🎨 GUI Layout

```
╔═══════════════════════════════════════════════════════════╗
║                    🔨 RPG Mender                          ║
╠═══════════════════════════════════════════════════════════╣
║  [Input]     [ ]     [ ]     [ ]    [Preview]    [ ]     ║
║  [ ]         [ ]     [ ]     [ ]     [ ]         [ ]     ║
║  [ ]         [ ]     [ ]     [ ]     [Cost]      [ ]     ║
║  [ ]         [ ]     [ ]     [ ]     [ ]         [ ]     ║
║  [RepairAll] [Repair] [Close]                            ║
╚═══════════════════════════════════════════════════════════╝
```

- **Left Slot (Input):** Place damaged item here
- **Center Slot (Preview):** See repaired item with cost
- **Right Slot (Cost):** Display repair cost
- **Bottom Buttons:** Repair All, Repair, Close

---

## 🔧 For Server Admins

### Troubleshooting

**MMOItems items not detected?**
- Ensure MMOItems is installed and loaded before OmniRepair
- Check `mmoitems.enabled: true` in config
- Verify the item is a valid MMOItem

**Economy not working?**
- Ensure Vault and an economy plugin are installed
- Check `economy.enabled: true` in config
- Verify player has sufficient funds

**NBT data lost after repair?**
- This should never happen with OmniRepair
- Enable debug mode: `/repair debug`
- Report any issues on GitHub

### Debug Mode

Enable debug logging for troubleshooting:

```yaml
settings:
  debug: true
```

Or use command: `/repair debug` (admin only)

---

## 🛠️ For Developers

### API Usage

OmniRepair provides a public API for integration with other plugins.

```java
// Get the OmniRepair instance
OmniRepair plugin = OmniRepair.getPlugin(OmniRepair.class);

// Check if an item can be repaired
boolean canRepair = plugin.getItemUtils().canRepair(itemStack);

// Get repair cost
double cost = plugin.getVanillaRepair().getRepairCost(itemStack);

// Perform repair
ItemStack repaired = plugin.getVanillaRepair().repair(itemStack, player);
```

### Soft Dependencies

OmniRepair uses soft-dependencies, meaning it will work without MMOItems or Vault:

```yaml
# plugin.yml
softdepend:
  - MMOItems
  - Vault
  - WorldGuard
```

---

## 📝 To-Do / Roadmap

- [ ] Oraxen integration
- [ ] ItemsAdder integration
- [ ] Custom repair recipes
- [ ] Repair animations
- [ ] MySQL stats tracking
- [ ] BungeeCord support
- [ ] Translation support (i18n)

---

## 📄 License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2024 Syaaddd

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🤝 Support

- **Issues:** [GitHub Issues](https://github.com/Syaaddd/OmniRepair/issues)
- **Discord:** [Join our Discord](https://discord.gg/your-invite)
- **Wiki:** [Plugin Wiki](https://github.com/Syaaddd/OmniRepair/wiki)

---

## 🙏 Credits

- **Author:** Syaaddd
- **Contributors:** [View contributors](https://github.com/Syaaddd/OmniRepair/graphs/contributors)
- **Special Thanks:** MMOItems team, Vault team, PaperMC team

---

**Enjoy repairing your items safely!** 🔨✨
#   O m n i R e p a i r  
 