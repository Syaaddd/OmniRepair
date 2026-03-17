# 🔨 OmniRepair

> **Repair Vanilla & RPG Items Safely. MMOItems Supported.**

<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.0-blue?style=flat-square" alt="Version">
  <img src="https://img.shields.io/badge/java-21+-orange?style=flat-square" alt="Java">
  <img src="https://img.shields.io/badge/platform-Paper%20%7C%20Spigot%20%7C%20Purpur-green?style=flat-square" alt="Platform">
  <img src="https://img.shields.io/badge/minecraft-1.21+-brightgreen?style=flat-square" alt="Minecraft">
  <img src="https://img.shields.io/badge/license-MIT-lightgrey?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/build-passing-success?style=flat-square" alt="Build">
</p>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Installation](#-installation)
- [Usage](#-usage)
- [Configuration](#-configuration)
- [GUI Guide](#-gui-guide)
- [Economy System](#-economy-system)
- [MMOItems Integration](#-mmoitems-integration)
- [Permissions](#-permissions)
- [Commands](#-commands)
- [Developer API](#-developer-api)
- [Building from Source](#-building-from-source)
- [Troubleshooting](#-troubleshooting)
- [Roadmap](#-roadmap)
- [License](#-license)
- [Support](#-support)

---

## 📖 Overview

**OmniRepair** is a comprehensive repair plugin designed for Minecraft RPG servers. It seamlessly handles both vanilla Minecraft items and custom RPG items from **MMOItems**, providing a unified, player-friendly repair experience with full economy integration and 100% NBT-safe operations.

### Why Choose OmniRepair?

| Problem | OmniRepair Solution |
|---------|---------------------|
| Vanilla anvil removes enchants | ✅ 100% NBT preservation |
| MMOItems not detected as damaged | ✅ MMOItems API integration |
| Manual repair is time-consuming | ✅ Interactive GUI with bulk repair |
| Unbalanced repair costs | ✅ Configurable economy system |
| Complex repair systems | ✅ Simple GUI-only interface |

---

## ✨ Features

### 🎮 Universal Durability Detection

- **Vanilla Items** — Tools, armor, weapons, elytra, trident, shield
- **MMOItems** — Full support via official API (not NBT parsing)
- **Automatic Lore Sync** — Update durability lore automatically after repair
- **Fallback System** — Runs even without MMOItems

### 🖥️ Simplified Repair GUI

```
┌─────────────────────────────────────────────────────────┐
│                   🔨 RPG Mender                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│   [Repair Hand]        [     ]     [Repair All]        │
│                                                         │
│   [     ]            [Close]         [     ]           │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

- **Repair Hand** — Repair item in hand instantly
- **Repair All** — Repair all damaged items in inventory
- **Close** — Close GUI with sound effect
- **Simple & Fast** — Only 3 buttons, no confusing input/preview slots
- **GUI-Only System** — All repairs must be done through the GUI interface

### 💰 Economy & Cost System

Three payment methods available:

| # | Method | Description | Example Display |
|---|--------|-------------|-----------------|
| 1 | **Vault Economy** | Integrates with any economy plugin | `$10.00` |
| 2 | **XP Levels** | Pay with experience levels | `5 XP Levels` |
| 3 | **Item Cost** | Pay with specific items | `10x Iron Ingot` |

### 🛡️ Safety & Protection

| Protection | Description |
|------------|-------------|
| **NBT Preservation** | Enchantments, custom model data, and lore remain intact |
| **Soulbound Check** | Respects soulbound items from other plugins |
| **Blacklist System** | Blocks specific materials, names, or lore |
| **Cost Cap** | Maximum repair cost limit prevents exploitation |
| **Rollback Safety** | Failed repairs restore item to original state |

---

## 📥 Installation

### Requirements

| Dependency | Version | Status |
|------------|---------|--------|
| Paper / Spigot / Purpur | 1.21+ | ✅ Required |
| Java | 21+ | ✅ Required |
| MMOItems | 6.9+ | ❌ Optional |
| Vault | 1.7+ | ❌ Optional |
| WorldGuard | 7.0+ | ❌ Optional |

### Quick Install

```
1. Download  →  OmniRepair-1.0.0-SNAPSHOT.jar from Releases page
2. Copy      →  Place JAR in server's plugins/ folder
3. Start     →  Run server to generate config files
4. Configure →  Edit plugins/OmniRepair/config.yml
5. Reload    →  /repair reload (optional)
```

### Starter Config

```yaml
# config.yml — Recommended starter configuration
settings:
  support-mmoitems: true      # Enable if using MMOItems
  use-economy: true           # Enable repair costs
  cost-per-percent: 10.0      # $10 per 1% durability lost
  max-cost: 5000.0            # Maximum $5000 per repair

mmoitems:
  enabled: true
  sync-lore: true
  custom-cost-multiplier: 1.5 # MMOItems 50% more expensive
```

---

## 🎮 Usage

### Basic Commands

#### Player Commands
```bash
/repair          # Open repair GUI for yourself
/repair help     # Show help message
```

#### Console/Admin Commands
```bash
/repair <player>    # Open repair GUI for a specific player
/repair reload      # Reload configuration (Admin)
/repair debug       # Toggle debug mode (Admin)
/repair help        # Show help message
```

> **Note:** Repair is **GUI-only**. Direct repair commands (`/repair hand`, `/repair all`) have been removed.

### GUI Workflow

```
┌──────────────────────────────────────────────────────────┐
│ Step 1: Admin/Console runs: /repair <player>             │
│            OR                                            │
│            Player runs: /repair                          │
├──────────────────────────────────────────────────────────┤
│ Step 2: Player clicks "Repair Hand" or "Repair All"      │
├──────────────────────────────────────────────────────────┤
│ Step 3: Item repaired with sound & particle effects!     │
└──────────────────────────────────────────────────────────┘
```

### Console Command Examples

```bash
# Open repair GUI for a player named "Steve"
/repair Steve

# Open repair GUI for a player named "Alex"
/repair Alex

# From server console:
console> repair Steve
console> repair Alex
```

### Use Cases

#### For Server Admins
```bash
# Help a player who needs repair
/repair PlayerName

# Open GUI for multiple players (one at a time)
/repair Player1
/repair Player2
```

#### For Players
```bash
# Open your own repair GUI
/repair
```

### Permission Required

| Permission | Description | Default |
|------------|-------------|---------|
| `omnirepair.use` | Open repair GUI (for players) | `true` |
| `omnirepair.admin` | Use admin commands (reload, debug, target players) | `op` |
| `omnirepair.free` | Free repair (bypass all costs) | `false` |

---

## ⚙️ Configuration

### 1. General Settings

```yaml
settings:
  debug: false                # Enable debug mode (console logging)
  support-mmoitems: true      # MMOItems support
  use-economy: true           # Economy integration

  # Cost configuration
  cost-per-percent: 10.0      # Cost per 1% durability lost
  max-cost: 5000.0            # Maximum repair cost
  min-cost: 5.0               # Minimum repair cost

  # GUI configuration
  max-bulk-repair: 360        # Max items in bulk repair (Repair All button)
```

### 2. Blacklist Configuration

```yaml
blacklist:
  # Materials that cannot be repaired
  materials:
    - BEDROCK
    - BARRIER
    - COMMAND_BLOCK
    - STRUCTURE_BLOCK
    - ENCHANTED_GOLDEN_APPLE
    - TOTEM_OF_UNDYING
    - ELYTRA           # Optional: protect elytra
    - TRIDENT          # Optional: protect trident

  # Items with specific lore (case-insensitive)
  lore-contains:
    - "&lEVENT ITEM"
    - "&cNO REPAIR"
    - "Unrepairable"
    - "[Event]"

  # Items with specific display name
  name-contains:
    - "Event Item"
    - "Admin Item"

  # MMOItems IDs to blacklist
  mmoitems-ids:
    - "EXAMPLE_SWORD_EVENT"
    - "EXAMPLE_ARMOR_BOSS"
```

### 3. MMOItems Integration

```yaml
mmoitems:
  enabled: true
  custom-cost-multiplier: 1.5   # 1.5 = 50% more expensive than vanilla

  # Automatically update durability lore after repair
  sync-lore: true

  # Lore format (placeholder: {current}, {max}, {percent})
  lore-format:
    enabled: true
    line: "&7Durability: &a{current} &7/ &a{max} &7(&e{percent}%&7)"
    position: "ADD_BOTTOM"      # ADD_BOTTOM | ADD_TOP | REPLACE_EXISTING

  # Lore patterns to replace (if using REPLACE_EXISTING)
  lore-patterns:
    - ".*Durability:.*"
    - ".*HP:.*"
```

### 4. Economy Options

```yaml
economy:
  enabled: true

  # Alternative: Pay with items
  use-item-cost: false
  cost-item:
    material: "IRON_INGOT"
    amount-per-percent: 0.01

  # Alternative: Pay with XP
  use-xp-cost: false
  xp-per-percent: 0.5

  # Permission for free repair
  free-repair-permission: "omnirepair.free"
```

### 5. Safety Settings

```yaml
safety:
  respect-soulbound: true
  soulbound-lore:
    - "Soulbound"
    - "Soul Bound"
    - "&9Soulbound"

  preserve-nbt: true            # Always preserve NBT (cannot be disabled)
  check-balance: true           # Check balance before repair
  max-durability-restore: 0     # 0 = unlimited
```

### 6. GUI Customization

```yaml
gui:
  title: "&8&l🔨 RPG Mender"
  size: 27                       # 3 rows for simplified GUI

  background: "BLACK_STAINED_GLASS_PANE"
  background-name: " "

  # Slot positions - Only 3 buttons
  slots:
    repair-hand: 10    # Left - Repair item in hand
    repair-all: 12     # Right - Repair all inventory
    close: 4           # Center - Close GUI

  # Button materials
  buttons:
    repair-hand: "ANVIL"
    repair-all: "HOPPER"
    close: "BARRIER"

  # Button names (supports color codes)
  button-names:
    repair-hand: "&a&lRepair Hand"
    repair-hand-lore:
      - ""
      - "&7Click to repair the item"
      - "&7in your hand."
      - ""
      - "&eCost: Based on damage"

    repair-all: "&e&lRepair All Inventory"
    repair-all-lore:
      - ""
      - "&7Click to repair all"
      - "&7damaged items in your"
      - "&7inventory."
      - ""
      - "&eCost: Based on total damage"

    close: "&c&lClose"
    close-lore:
      - ""
      - "&7Close this GUI"
```

### 7. Visual Effects

```yaml
effects:
  sound:
    enabled: true
    type: "BLOCK_ANVIL_USE"
    volume: 1.0
    pitch: 1.0

  particles:
    enabled: true
    type: "VILLAGER_HAPPY"
    count: 10
    offset-x: 0.5
    offset-y: 0.5
    offset-z: 0.5

  action-bar:
    enabled: true
    message: "&a✓ Item Repaired! Cost: ${cost}"
```

### 8. WorldGuard Integration

```yaml
worldguard:
  enabled: true

  # Region yang menonaktifkan repair
  disabled-regions:
    - "repair-disabled-zone"
    - "pvp-arena"

  # Izinkan repair hanya di region tertentu (kosong = semua diizinkan)
  allowed-regions: []
```

---

## 🎨 GUI Guide

### Simplified GUI Layout (27-Slot - 3 Rows)

```
Row 1:  [ 0][ 1][ 2][ 3][ 4][ 5][ 6][ 7][ 8]
Row 2:  [ 9][10][11][12][13][14][15][16][26]
Row 3:  [18][19][20][21][22][23][24][25][26]
```

### Slot Assignments (Default)

| Slot | Position | Function |
|------|----------|----------|
| **10** | Row 2, Col 2 | **Repair Hand** — Repair item in hand |
| **12** | Row 2, Col 4 | **Repair All** — Repair all inventory items |
| **4**  | Row 1, Col 5 | **Close** — Close GUI |

### Button Functions

| Button | Description |
|--------|-------------|
| **Repair Hand** | Repair held item instantly |
| **Repair All** | Repair all damaged items in inventory |
| **Close** | Close GUI with sound effect |

---

## 💰 Economy System

### Cost Calculation Formula

```
Base Cost  = Damage Percent × Cost Per Percent
Final Cost = Base Cost × MMOItems Multiplier (if applicable)
           → capped between Min Cost and Max Cost
```

### Calculation Examples

**Vanilla Diamond Sword:**

```
Durability    : 500 / 1562
Damage        : 1062 (68%)
Cost/Percent  : $10.00
Calculation   : 68 × $10 = $680.00
```

**MMOItems Legendary Sword:**

```
Durability    : 200 / 1000
Damage        : 800 (80%)
Cost/Percent  : $10.00
MMO Multiplier: 1.5×
Calculation   : 80 × $10 × 1.5 = $1,200.00
```

### Payment Method Comparison

| Method | Advantages | Disadvantages | Best For |
|--------|------------|---------------|----------|
| **Money** | Simple, universal | Requires economy plugin | RPG, Skyblock |
| **XP** | Vanilla-friendly | Grind for players | Survival, Hardcore |
| **Items** | Immersive | Requires farming | Tech, Magic servers |

---

## ⚔️ MMOItems Integration

### Workflow

```
1. Detection  →  Check if item is MMOItem via MMOItems.getID()
2. Read       →  Get current/max durability via MMOItems API
3. Repair     →  Restore durability via MMOItems API
4. Sync       →  Update durability lore automatically
```

### Supported Features

| Feature | Status |
|---------|--------|
| Custom Durability | ✅ Full support |
| Lore Synchronization | ✅ Full support |
| Type-based Repair | ✅ Full support |
| Stat-based Items | ✅ Full support |
| Custom Models | ✅ Full support |

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `omnirepair.use` | Open repair GUI | `true` (all players) |
| `omnirepair.admin` | Admin commands (`reload`, `debug`, target players) | `op` |
| `omnirepair.free` | Free repair (bypass all costs) | `false` |

### Permission Setup Examples

**Survival Server:**

```yaml
permissions:
  omnirepair.use: true      # All players can use repair GUI
  omnirepair.admin: false   # Only OP can use admin commands
```

**RPG Server (with VIP):**

```yaml
groups:
  vip:
    permissions:
      - omnirepair.use
      - omnirepair.free     # VIP gets free repair!
  default:
    permissions:
      - omnirepair.use
```

---

## 💬 Commands

### Player Commands

| Command | Description | Permission | Usage Example |
|---------|-------------|------------|---------------|
| `/repair` | Open repair GUI for yourself | `omnirepair.use` | `/repair` |
| `/repair help` | Show help message | `omnirepair.use` | `/repair help` |

### Console/Admin Commands

| Command | Description | Permission | Usage Example |
|---------|-------------|------------|---------------|
| `/repair <player>` | Open repair GUI for a player | `omnirepair.admin` | `/repair Steve` |
| `/repair reload` | Reload configuration files | `omnirepair.admin` | `/repair reload` |
| `/repair debug` | Toggle debug mode | `omnirepair.admin` | `/repair debug` |
| `/repair help` | Show help message | `omnirepair.use` | `/repair help` |

### Command Examples

#### Player Usage
```bash
# Open your own repair GUI
/repair
```

#### Admin Usage
```bash
# Open repair GUI for a specific player
/repair Steve

# Reload configuration
/repair reload

# Toggle debug mode
/repair debug
```

#### Console Usage
```bash
# Open repair GUI for a player (from server console)
repair Steve
```

### Aliases

```
/repair  =  /omnirepair  =  /fix
```

---

## 🛠️ Developer API

### Setup Dependency (Maven)

```xml
<repository>
    <id>jitpack-repo</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.Syaaddd</groupId>
    <artifactId>OmniRepair</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### Mendapatkan Plugin Instance

```java
import com.github.Syaaddd.omniRepair.OmniRepair;

OmniRepair plugin = OmniRepair.getPlugin(OmniRepair.class);
```

### Cek Status Item

```java
ItemStack item = player.getInventory().getItemInMainHand();

boolean canRepair     = plugin.getItemUtils().canRepair(item);
boolean isDamaged     = plugin.getItemUtils().isDamaged(item);
boolean isBlacklisted = plugin.getItemUtils().isBlacklisted(item);
boolean isSoulbound   = plugin.getItemUtils().isSoulbound(item);
```

### Informasi Durability

```java
double current       = plugin.getItemUtils().getCurrentDurability(item);
double max           = plugin.getItemUtils().getMaxDurability(item);
double percent       = plugin.getItemUtils().getDurabilityPercent(item);
double damagePercent = plugin.getItemUtils().getDamagePercent(item);
```

### Kalkulasi & Eksekusi Repair

```java
// Kalkulasi biaya
double vanillaCost = plugin.getVanillaRepair().getRepairCost(item);
double mmoCost     = plugin.getMmoItemsRepair().getRepairCost(item);

// Repair dengan player (cek economy, efek, pesan)
ItemStack repaired = plugin.getVanillaRepair().repair(item, player);

// Repair tanpa player (silent)
ItemStack repaired = plugin.getVanillaRepair().repair(item);

// Repair MMOItems
ItemStack repaired = plugin.getMmoItemsRepair().repair(item, player);
```

### Economy Integration

```java
import com.github.Syaaddd.omniRepair.economy.EconomyHandler;

EconomyHandler economy = plugin.getEconomyHandler();

boolean canAfford    = economy.canAfford(player, cost);
boolean success      = economy.withdraw(player, cost);
PaymentMethod method = economy.getPaymentMethod(); // MONEY | XP | ITEMS | FREE
```

### Contoh: Custom Repair Command

```java
@Command("customrepair")
public void onCustomRepair(Player player) {
    ItemStack item = player.getInventory().getItemInMainHand();

    if (!plugin.getItemUtils().canRepair(item)) {
        player.sendMessage("Item ini tidak bisa direpair!");
        return;
    }

    double cost = plugin.getVanillaRepair().getRepairCost(item);

    if (!plugin.getEconomyHandler().canAfford(player, cost)) {
        player.sendMessage("Saldo tidak mencukupi!");
        return;
    }

    ItemStack repaired = plugin.getVanillaRepair().repair(item, player);
    if (repaired != null) {
        plugin.getEconomyHandler().withdraw(player, cost);
        player.getInventory().setItemInMainHand(repaired);
        player.sendMessage("Item berhasil direpair!");
    }
}
```

---

## 🏗️ Building from Source

### Prerequisites

- Java JDK 21+
- Maven 3.8+ atau IntelliJ IDEA
- Git

### Build via Maven CLI

```bash
# Clone repository
git clone https://github.com/Syaaddd/OmniRepair.git
cd OmniRepair

# Build project
mvn clean package

# Output: target/OmniRepair-1.0.0-SNAPSHOT.jar
```

### Build via IntelliJ IDEA

1. Buka project di IntelliJ IDEA
2. Tunggu Maven selesai import dependencies
3. **Build → Build Project** (`Ctrl+F9`)
4. JAR tersedia di `target/OmniRepair-1.0.0-SNAPSHOT.jar`

### Troubleshooting Build

```bash
# Java tidak ditemukan
export JAVA_HOME=/path/to/jdk-21

# Dependencies tidak ditemukan
mvn dependency:purge-local-repository
mvn clean package
```

---

## 🔧 Troubleshooting

### Plugin Tidak Mau Load

**Error:** `Could not load plugin`

- Pastikan Java 21+ terinstall
- Gunakan Paper/Spigot 1.21+
- Cek console untuk pesan error lengkap

### MMOItems Items Tidak Bisa Direpair

**Error:** Item tampak "tidak rusak"

```yaml
# Verifikasi urutan load plugin:
# 1. MMOItems harus load lebih dulu
# 2. Baru OmniRepair

# Aktifkan debug mode:
/repair debug

# Cari di console: "✓ MMOItems integration enabled"
```

### Economy Tidak Berfungsi

**Error:** "Insufficient funds" atau repair selalu gratis

```yaml
# Pastikan Vault terinstall dan konfigurasi benar:
economy:
  enabled: true
  use-economy: true
```

### GUI Tidak Terbuka

- Cek permission: `omnirepair.use`
- Pastikan tidak ada plugin yang konflik
- Coba `/repair reload`

### NBT / Enchantment Hilang

> ⚠️ Ini **seharusnya tidak terjadi**. Aktifkan debug mode dan laporkan ke GitHub Issues.

```yaml
settings:
  debug: true
```

---

## 📝 Roadmap

### ✅ v1.0.0 — Current Release

- Vanilla item repair
- MMOItems integration
- Interactive GUI
- Economy integration (Money / XP / Items)
- Bulk repair
- Blacklist system
- NBT & enchantment protection

### 🔜 v1.1.0 — Planned

- [ ] Oraxen integration
- [ ] ItemsAdder integration
- [ ] Custom repair recipes
- [ ] Repair cooldown system
- [ ] MySQL statistics logging

### 🔮 v1.2.0 — Planned

- [ ] Repair animations
- [ ] Sound & particle customization
- [ ] Multi-language support (i18n)
- [ ] BungeeCord support

### 🚀 v2.0.0 — Future

- [ ] Web interface for admins
- [ ] Extended API for custom repair types
- [ ] Discord webhook notifications
- [ ] Auction house & shop integration

---

## 📄 License

Proyek ini dilisensikan di bawah **MIT License**.

**Singkatnya:**
- ✅ Boleh digunakan di server apapun (personal maupun komersial)
- ✅ Boleh dimodifikasi dan didistribusikan
- ❌ Tidak boleh mengklaim sebagai karya sendiri (tanpa izin)
- ❌ Penulis tidak bertanggung jawab atas kerusakan apapun

Lihat file [LICENSE](LICENSE) untuk teks lengkap.

---

## 🤝 Support

### Mendapatkan Bantuan

| Jenis Masalah | Tempat Bertanya |
|---------------|-----------------|
| Bug Reports | [GitHub Issues](https://github.com/Syaaddd/OmniRepair/issues) |
| Feature Requests | [GitHub Issues](https://github.com/Syaaddd/OmniRepair/issues) |
| Pertanyaan Umum | Discord (coming soon) |
| Bantuan Konfigurasi | Discord / GitHub Discussions |

### Melaporkan Bug

Sertakan informasi berikut saat melaporkan bug:

1. Versi server (`/version`)
2. Versi plugin (`/plugins`)
3. Daftar plugin aktif
4. Isi `config.yml`
5. Log console (pesan error lengkap)
6. Langkah untuk mereproduksi masalah

### Links

- 🔗 **GitHub:** [Syaaddd/OmniRepair](https://github.com/Syaaddd/OmniRepair)
- 💬 **Discord:** Coming soon
- 📦 **Modrinth:** Coming soon
- 🔌 **SpigotMC:** Coming soon

---

## 🙏 Credits

**Author:** [Syaaddd](https://github.com/Syaaddd)

**Special Thanks:**
- MMOItems Team — Dokumentasi API yang sangat baik
- Vault Team — Abstraksi economy yang solid
- PaperMC Team — Software server yang performan
- Komunitas — Testing dan feedback

**Dependencies:**

| Library | Lisensi | Kegunaan |
|---------|---------|----------|
| PaperMC API | GPL-3.0 | Server API |
| MMOItems | Proprietary | RPG item support |
| Vault API | GPL-3.0 | Economy abstraction |
| WorldGuard | GPL-3.0 | Region protection |

---

<div align="center">

**Enjoy repairing your items safely!** 🔨✨

Made with ❤️ by [Syaaddd](https://github.com/Syaaddd)

[🐛 Report Bug](https://github.com/Syaaddd/OmniRepair/issues) · [💡 Request Feature](https://github.com/Syaaddd/OmniRepair/issues) · [⬇️ Download](https://github.com/Syaaddd/OmniRepair/releases)

</div>
