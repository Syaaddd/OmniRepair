# 🔨 OmniRepair

**Repair Vanilla & RPG Items Safely. MMOItems Supported.**

[![Version](https://img.shields.io/badge/version-1.0.0-blue)]()
[![Java](https://img.shields.io/badge/java-21-orange)]()
[![Platform](https://img.shields.io/badge/platform-paper%2Fspigot%2Fpurpur-green)]()
[![License](https://img.shields.io/badge/license-MIT-lightgrey)]()
[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Installation](#-installation)
- [Usage](#-usage)
- [Configuration](#-configuration)
- [GUI Guide](#-gui-guide)
- [Economy System](#-economy-system)
- [MMOItems Integration](#-mmoitems-integration)
- [Troubleshooting](#-troubleshooting)
- [Developer API](#-developer-api)
- [Permissions](#-permissions)
- [Commands](#-commands)
- [Building from Source](#-building-from-source)
- [Roadmap](#-roadmap)
- [License](#-license)
- [Support](#-support)

---

## 📖 Overview

**OmniRepair** is a comprehensive repair plugin designed for Minecraft RPG servers. It seamlessly handles both vanilla Minecraft items and custom RPG items from **MMOItems**, providing a unified, player-friendly repair experience with full economy integration and 100% NBT-safe operations.

### Why Choose OmniRepair?

| Problem | OmniRepair Solution |
|---------|---------------------|
| Anvil vanila menghapus enchant | ✅ 100% NBT preservation |
| Item MMOItems tidak terdeteksi rusak | ✅ MMOItems API integration |
| Repair manual memakan waktu | ✅ Bulk repair & GUI interaktif |
| Biaya repair tidak seimbang | ✅ Configurable economy system |

---

## ✨ Features

### 🎮 Universal Durability Detection

- **Vanilla Items** - Tools, armor, weapons, elytra, trident, shield
- **MMOItems** - Full support via official API (not NBT parsing)
- **Automatic Lore Sync** - Updates durability lore after repair
- **Fallback System** - Gracefully degrades if MMOItems unavailable

### 🖥️ Smart Repair GUI

```
┌─────────────────────────────────────────────────────────┐
│                   🔨 RPG Mender                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│   ┌─────┐                  ┌─────┐     ┌─────┐        │
│   │Input│  →  [Preview] →  │Cost │     │ ... │        │
│   └─────┘                  └─────┘     └─────┘        │
│                                                         │
│         [🛠️ Repair All]  [🔨 Repair]  [❌ Close]       │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

- **Live Preview** - See repaired item before paying
- **Cost Display** - Transparent pricing upfront
- **Bulk Repair** - Repair entire inventory at once
- **RPG Theme** - Immersive dark theme with custom styling

### 💰 Economy & Cost System

**Three Payment Methods:**

1. **Vault Economy** (Money)
   - Integrates with any Vault-compatible economy
   - Configurable cost per durability percent
   - Format: `$10.00` displayed in GUI

2. **XP Levels**
   - Pay with experience levels instead of money
   - Great for hardcore/survival servers
   - Format: `5 XP Levels`

3. **Item Cost**
   - Pay with specific items (e.g., Iron Ingots)
   - Perfect for tech/magic servers
   - Format: `10x Iron Ingot`

### 🛡️ Safety Protection

| Protection | Description |
|------------|-------------|
| **NBT Preservation** | All enchantments, custom model data, lore preserved |
| **Soulbound Check** | Respects soulbound items from other plugins |
| **Blacklist System** | Block specific materials, names, or lore |
| **Cost Cap** | Maximum repair cost prevents exploitation |
| **Rollback Safety** | Failed repairs restore original item |

---

## 📥 Installation

### Requirements

| Requirement | Version | Required |
|-------------|---------|----------|
| **Server Software** | Paper/Spigot/Purpur 1.21+ | ✅ Required |
| **Java** | Java 21+ | ✅ Required |
| **MMOItems** | 6.9+ | ❌ Optional |
| **Vault** | 1.7+ | ❌ Optional |
| **WorldGuard** | 7.0+ | ❌ Optional |

### Quick Install

1. **Download** the latest JAR from [Releases](https://github.com/Syaaddd/OmniRepair/releases)

2. **Copy** to your server's plugins folder:
   ```
   OmniRepair-1.0.0-SNAPSHOT.jar → plugins/OmniRepair/
   ```

3. **Start** your server to generate config files

4. **Configure** in `plugins/OmniRepair/config.yml`

5. **Reload** (optional):
   ```
   /repair reload
   ```

### First-Time Setup

```yaml
# config.yml - Recommended starter config
settings:
  support-mmoitems: true      # Enable if using MMOItems
  use-economy: true           # Enable repair costs
  cost-per-percent: 10.0      # $10 per 1% durability lost
  max-cost: 5000.0            # Maximum $5000 per repair
  
mmoitems:
  enabled: true
  sync-lore: true
  custom-cost-multiplier: 1.5 # MMOItems cost 50% more
```

---

## 🎮 Usage

### For Players

**Opening the GUI:**
```
/repair
```

**Quick Repair (held item):**
```
/repair hand
```

**Repair All Inventory:**
```
/repair all
```

### GUI Workflow

1. **Place** damaged item in **Input Slot** (left)
2. **View** preview and cost in center/right slots
3. **Click** the **🔨 Repair** button
4. **Collect** your fully repaired item!

**Example:**
```
Input: Diamond Sword (Durability: 500/1562)
Preview: Diamond Sword (Durability: 1562/1562)
Cost: $106.50
```

---

## ⚙️ Configuration

### Full Configuration Reference

#### 1. **General Settings**

```yaml
settings:
  # Enable debug mode (console logging)
  debug: false
  
  # MMOItems support
  support-mmoitems: true
  
  # Economy integration
  use-economy: true
  
  # Cost configuration
  cost-per-percent: 10.0    # Cost per 1% durability lost
  max-cost: 5000.0          # Maximum single repair cost
  min-cost: 5.0             # Minimum repair cost
  
  # Feature toggles
  repair-held-item: true    # Allow /repair hand
  bulk-repair: true         # Allow /repair all
  max-bulk-repair: 360      # Max items in bulk repair
```

#### 2. **Blacklist Configuration**

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
    - ELYTRA               # Optional: protect elytra
    - TRIDENT              # Optional: protect trident
  
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

#### 3. **MMOItems Integration**

```yaml
mmoitems:
  # Enable MMOItems API integration
  enabled: true
  
  # Cost multiplier for MMOItems (1.5 = 50% more expensive)
  custom-cost-multiplier: 1.5
  
  # Automatically update durability lore after repair
  sync-lore: true
  
  # Lore format (placeholders: {current}, {max}, {percent})
  lore-format:
    enabled: true
    line: "&7Durability: &a{current} &7/ &a{max} &7(&e{percent}%&7)"
    position: "ADD_BOTTOM"  # ADD_BOTTOM, ADD_TOP, REPLACE_EXISTING
  
  # Existing lore patterns to replace (if using REPLACE_EXISTING)
  lore-patterns:
    - ".*Durability:.*"
    - ".*Durability.*"
    - ".*HP:.*"
```

#### 4. **Economy Options**

```yaml
economy:
  # Enable Vault economy
  enabled: true
  
  # Alternative: Use item cost instead of money
  use-item-cost: false
  cost-item:
    material: "IRON_INGOT"
    amount-per-percent: 0.01  # Amount per 1% durability lost
  
  # Alternative: Use XP levels
  use-xp-cost: false
  xp-per-percent: 0.5  # XP levels per 1% durability lost
  
  # Free repair permission
  free-repair-permission: "omnirepair.free"
```

#### 5. **Safety Settings**

```yaml
safety:
  # Respect soulbound items
  respect-soulbound: true
  soulbound-lore:
    - "Soulbound"
    - "Soul Bound"
    - "&9Soulbound"
  
  # Always preserve NBT (cannot be disabled)
  preserve-nbt: true
  
  # Check player balance before repair
  check-balance: true
  
  # Maximum durability restore (0 = unlimited)
  max-durability-restore: 0
```

#### 6. **GUI Customization**

```yaml
gui:
  # GUI title (supports color codes)
  title: "&8&l🔨 RPG Mender"
  
  # GUI size (must be multiple of 9, max 54)
  size: 54
  
  # Background appearance
  background: "BLACK_STAINED_GLASS_PANE"
  background-name: " "
  
  # Slot positions
  slots:
    input: 10
    preview: 16
    cost: 28
    repair-button: 31
    repair-all-button: 29
    close-button: 33
  
  # Button materials
  buttons:
    repair: "ANVIL"
    repair-all: "HOPPER"
    close: "BARRIER"
    cost-display: "GOLD_BLOCK"
  
  # Button names
  button-names:
    repair: "&a&lREPAIR ITEM"
    repair-all: "&e&lRepair All Inventory"
    close: "&c&lClose"
```

#### 7. **Visual Effects**

```yaml
effects:
  # Sound on successful repair
  sound:
    enabled: true
    type: "BLOCK_ANVIL_USE"
    volume: 1.0
    pitch: 1.0
  
  # Particles on successful repair
  particles:
    enabled: true
    type: "VILLAGER_HAPPY"
    count: 10
    offset-x: 0.5
    offset-y: 0.5
    offset-z: 0.5
    speed: 0.5
  
  # Action bar message
  action-bar:
    enabled: true
    message: "&a✓ Item Repaired! Cost: ${cost}"
```

#### 8. **WorldGuard Integration**

```yaml
worldguard:
  enabled: true
  
  # Regions where repair is disabled
  disabled-regions:
    - "repair-disabled-zone"
    - "pvp-arena"
  
  # Allow repair only in specific regions (empty = all allowed)
  allowed-regions: []
```

---

## 🎨 GUI Guide

### Slot Layout (54-slot GUI)

```
Row 1:  [0]  [1]  [2]  [3]  [4]  [5]  [6]  [7]  [8]
Row 2:  [9]  [10] [11] [12] [13] [14] [15] [16] [17]
Row 3:  [18] [19] [20] [21] [22] [23] [24] [25] [26]
Row 4:  [27] [28] [29] [30] [31] [32] [33] [34] [35]
Row 5:  [36] [37] [38] [39] [40] [41] [42] [43] [44]
Row 6:  [45] [46] [47] [48] [49] [50] [51] [52] [53]
```

### Key Slots (Default)

| Slot | Position | Purpose |
|------|----------|---------|
| **10** | Row 2, Col 2 | **Input** - Place damaged item |
| **16** | Row 2, Col 8 | **Preview** - See repaired item |
| **28** | Row 4, Col 2 | **Cost Display** - Shows repair cost |
| **29** | Row 4, Col 3 | **Repair All Button** - Bulk repair |
| **31** | Row 4, Col 5 | **Repair Button** - Single repair |
| **33** | Row 4, Col 7 | **Close Button** - Exit GUI |

### Customizing GUI

Edit `config.yml` to change slot positions:

```yaml
gui:
  slots:
    input: 10        # Change to any slot 0-53
    preview: 16
    cost: 28
    repair-button: 31
    repair-all-button: 29
    close-button: 33
```

---

## 💰 Economy System

### Cost Calculation Formula

```
Base Cost = (Damage Percent) × (Cost Per Percent)

Final Cost = Base Cost × MMOItems Multiplier (if applicable)
             clamped between Min Cost and Max Cost
```

### Examples

**Example 1: Vanilla Diamond Sword**
```
Current Durability: 500/1562
Damage: 1062 (68% damaged)
Cost Per Percent: $10.00
Calculation: 68 × $10 = $680
Final Cost: $680.00
```

**Example 2: MMOItems Legendary Sword**
```
Current Durability: 200/1000
Damage: 800 (80% damaged)
Cost Per Percent: $10.00
MMOItems Multiplier: 1.5x
Calculation: 80 × $10 × 1.5 = $1200
Final Cost: $1200.00 (capped at max-cost if exceeded)
```

### Payment Methods Comparison

| Method | Pros | Cons | Best For |
|--------|------|------|----------|
| **Money** | Simple, universal | Requires economy plugin | RPG, Skyblock |
| **XP** | Vanilla-friendly | Grindy for players | Survival, Hardcore |
| **Items** | Immersive | Requires farming | Tech, Magic servers |

---

## ⚔️ MMOItems Integration

### How It Works

1. **Detection**: Plugin checks if item is MMOItem via `MMOItems.getID()`
2. **Durability Read**: Uses MMOItems API to get current/max durability
3. **Repair**: Calls MMOItems API to restore durability
4. **Lore Sync**: Updates durability lore automatically

### Supported MMOItems Features

| Feature | Support |
|---------|---------|
| Custom Durability | ✅ Full |
| Lore Synchronization | ✅ Full |
| Type-based Repair | ✅ Full |
| Stat-based Items | ✅ Full |
| Custom Models | ✅ Full |

### Troubleshooting MMOItems

**Problem: MMOItems not detected**

```yaml
# Check these settings:
mmoitems:
  enabled: true  # Must be true
  
# Verify in console:
# Look for: "✓ MMOItems integration enabled"
```

**Problem: Durability not updating**

```yaml
# Enable lore sync:
mmoitems:
  sync-lore: true
  
# Check lore format:
mmoitems:
  lore-format:
    enabled: true
```

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Plugin Won't Load

**Error:** `Could not load plugin`

**Solution:**
- Ensure Java 21+ is installed
- Use Paper/Spigot 1.21+
- Check console for full error message

#### 2. MMOItems Items Not Repairing

**Error:** Items show as "not damaged"

**Solution:**
```yaml
# Verify MMOItems is loaded first
# Check load order in server startup:
1. MMOItems
2. OmniRepair

# Enable debug mode:
/repair debug

# Check console for:
# "✓ MMOItems integration enabled"
```

#### 3. Economy Not Working

**Error:** "Insufficient funds" or free repair

**Solution:**
```yaml
# Ensure Vault is installed
# Check economy enabled:
economy:
  enabled: true
  use-economy: true

# Verify player balance:
/vault-deposit <player> 1000
```

#### 4. GUI Not Opening

**Error:** Nothing happens on `/repair`

**Solution:**
- Check permission: `omnirepair.use`
- Ensure no other plugin conflicts
- Try `/repair reload`

#### 5. NBT Data Lost

**Error:** Enchantments missing after repair

**Solution:**
```yaml
# This should NEVER happen
# Enable debug to investigate:
settings:
  debug: true

# Report to GitHub with:
# - Server version
# - Plugin list
# - Console logs
```

### Debug Mode

Enable detailed logging:

```yaml
settings:
  debug: true
```

Or use command:
```
/repair debug
```

**Debug output includes:**
- MMOItems API detection
- Economy transactions
- NBT verification results
- GUI events

---

## 🛠️ Developer API

### Accessing OmniRepair API

```java
import com.github.Syaaddd.omniRepair.OmniRepair;

// Get plugin instance
OmniRepair plugin = OmniRepair.getPlugin(OmniRepair.class);
```

### Checking If Item Can Be Repaired

```java
import org.bukkit.inventory.ItemStack;

ItemStack item = player.getInventory().getItemInMainHand();

boolean canRepair = plugin.getItemUtils().canRepair(item);
boolean isDamaged = plugin.getItemUtils().isDamaged(item);
boolean isBlacklisted = plugin.getItemUtils().isBlacklisted(item);
boolean isSoulbound = plugin.getItemUtils().isSoulbound(item);
```

### Getting Durability Information

```java
double current = plugin.getItemUtils().getCurrentDurability(item);
double max = plugin.getItemUtils().getMaxDurability(item);
double percent = plugin.getItemUtils().getDurabilityPercent(item);
double damagePercent = plugin.getItemUtils().getDamagePercent(item);
```

### Calculating Repair Cost

```java
// Vanilla items
double cost = plugin.getVanillaRepair().getRepairCost(item);

// MMOItems
double cost = plugin.getMmoItemsRepair().getRepairCost(item);
```

### Performing Repair

```java
// With player (economy check, effects, messages)
ItemStack repaired = plugin.getVanillaRepair().repair(item, player);

// Without player (silent repair)
ItemStack repaired = plugin.getVanillaRepair().repair(item);

// MMOItems
ItemStack repaired = plugin.getMmoItemsRepair().repair(item, player);
```

### Economy Integration

```java
import com.github.Syaaddd.omniRepair.economy.EconomyHandler;

EconomyHandler economy = plugin.getEconomyHandler();

// Check if player can afford
boolean canAfford = economy.canAfford(player, cost);

// Withdraw payment
boolean success = economy.withdraw(player, cost);

// Get payment method
PaymentMethod method = economy.getPaymentMethod();
// MONEY, XP, ITEMS, or FREE
```

### Example: Custom Repair Command

```java
@Command("customrepair")
public void onCustomRepair(Player player) {
    ItemStack item = player.getInventory().getItemInMainHand();
    
    if (!plugin.getItemUtils().canRepair(item)) {
        player.sendMessage("This item cannot be repaired!");
        return;
    }
    
    double cost = plugin.getVanillaRepair().getRepairCost(item);
    
    if (!plugin.getEconomyHandler().canAfford(player, cost)) {
        player.sendMessage("Insufficient funds!");
        return;
    }
    
    ItemStack repaired = plugin.getVanillaRepair().repair(item, player);
    if (repaired != null) {
        plugin.getEconomyHandler().withdraw(player, cost);
        player.getInventory().setItemInMainHand(repaired);
        player.sendMessage("Item repaired!");
    }
}
```

### Maven Dependency

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

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `omnirepair.use` | Open repair GUI | `true` (all players) |
| `omnirepair.hand` | Use `/repair hand` | `true` (all players) |
| `omnirepair.bulk` | Use `/repair all` | `false` (OP only) |
| `omnirepair.admin` | Admin commands (`reload`, `debug`) | `op` |
| `omnirepair.free` | Free repairs (bypass all costs) | `false` |

### Permission Setup Examples

**Basic Setup (Survival Server):**
```yaml
# All players can use GUI and hand repair
# Only OPs can bulk repair
permissions:
  omnirepair.use: true
  omnirepair.hand: true
  omnirepair.bulk: false
```

**Premium Setup (RPG Server):**
```yaml
# VIP players get free repair
# Regular players pay
groups:
  vip:
    permissions:
      - omnirepair.use
      - omnirepair.hand
      - omnirepair.bulk
      - omnirepair.free  # Free repairs!
  default:
    permissions:
      - omnirepair.use
      - omnirepair.hand
```

---

## 💬 Commands

### Player Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/repair` | Open repair GUI | `omnirepair.use` |
| `/repair hand` | Repair held item instantly | `omnirepair.hand` |
| `/repair all` | Repair all inventory items | `omnirepair.bulk` |
| `/repair help` | Show help message | `omnirepair.use` |

### Admin Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/repair reload` | Reload configuration files | `omnirepair.admin` |
| `/repair debug` | Toggle debug mode | `omnirepair.admin` |

### Command Aliases

```yaml
# plugin.yml
commands:
  repair:
    aliases: [omnirepair, fix]
```

**All of these work:**
```
/repair
/omnirepair
/fix
```

---

## 🏗️ Building from Source

### Prerequisites

- **Java JDK 21** or higher
- **Maven 3.8+** or IntelliJ IDEA
- **Git** (for cloning)

### Build Steps

**Option 1: Using IntelliJ IDEA (Recommended)**

1. Open project in IntelliJ IDEA
2. Wait for Maven to import dependencies
3. Click **Build → Build Project** (or `Ctrl+F9`)
4. Find JAR in `target/OmniRepair-1.0.0-SNAPSHOT.jar`

**Option 2: Using Maven CLI**

```bash
# Clone repository
git clone https://github.com/Syaaddd/OmniRepair.git
cd OmniRepair

# Build project
mvn clean package

# Output JAR
target/OmniRepair-1.0.0-SNAPSHOT.jar
```

**Option 3: Using IntelliJ Maven (Windows)**

```powershell
cd "C:\Path\To\OmniRepair"
$env:JAVA_HOME="C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.1.1\jbr"
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.1.1\plugins\maven\lib\maven3\bin\mvn.cmd" clean package
```

### Build Output

```
[INFO] BUILD SUCCESS
[INFO] Building jar: C:\...\OmniRepair\target\OmniRepair-1.0.0-SNAPSHOT.jar
```

### Troubleshooting Build

**Error: Java not found**
```bash
# Set JAVA_HOME
export JAVA_HOME=/path/to/jdk-21
```

**Error: Dependencies not found**
```bash
# Clear Maven cache
mvn dependency:purge-local-repository
mvn clean package
```

---

## 📝 Roadmap

### Version 1.0.0 (Current)

- ✅ Vanilla item repair
- ✅ MMOItems integration
- ✅ GUI system
- ✅ Economy integration
- ✅ Bulk repair
- ✅ Blacklist system
- ✅ NBT protection

### Version 1.1.0 (Planned)

- [ ] Oraxen integration
- [ ] ItemsAdder integration
- [ ] Custom repair recipes
- [ ] Repair cooldown system
- [ ] MySQL statistics

### Version 1.2.0 (Planned)

- [ ] Repair animations
- [ ] Sound customization
- [ ] Particle effects customization
- [ ] Multi-language support (i18n)
- [ ] BungeeCord support

### Version 2.0.0 (Future)

- [ ] Web interface for admins
- [ ] API for custom repair types
- [ ] Shop integration
- [ ] Auction house integration
- [ ] Discord webhook notifications

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

### What This Means

✅ **You can:**
- Use on any server (personal or commercial)
- Modify the code
- Distribute copies
- Use in modpacks

❌ **You cannot:**
- Hold authors liable
- Claim as your own (without permission)

---

## 🤝 Support

### Getting Help

| Issue Type | Where to Get Help |
|------------|-------------------|
| Bug Reports | [GitHub Issues](https://github.com/Syaaddd/OmniRepair/issues) |
| Feature Requests | [GitHub Issues](https://github.com/Syaaddd/OmniRepair/issues) |
| General Questions | Discord (below) |
| Configuration Help | Discord or GitHub Discussions |

### Community Links

- **GitHub:** [Syaaddd/OmniRepair](https://github.com/Syaaddd/OmniRepair)
- **Discord:** [Join our Discord](https://discord.gg/your-invite) *(coming soon)*
- **Modrinth:** [OmniRepair on Modrinth](https://modrinth.com/plugin/omnirepair) *(coming soon)*
- **SpigotMC:** [OmniRepair on Spigot](https://www.spigotmc.org/resources/omnirepair) *(coming soon)*

### Reporting Bugs

When reporting a bug, please include:

1. **Server version** (`/version`)
2. **Plugin version** (`/plugins | grep OmniRepair`)
3. **Plugin list** (`/plugins`)
4. **Config file** (`config.yml`)
5. **Console logs** (full error message)
6. **Steps to reproduce**

### Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

---

## 🙏 Credits

### Author

- **Syaaddd** - Lead Developer

### Contributors

- View all [contributors](https://github.com/Syaaddd/OmniRepair/graphs/contributors)

### Special Thanks

- **MMOItems Team** - For excellent API documentation
- **Vault Team** - For economy abstraction
- **PaperMC Team** - For performant server software
- **Community** - For testing and feedback

### Dependencies

| Dependency | License | Purpose |
|------------|---------|---------|
| PaperMC API | GPL-3.0 | Server API |
| MMOItems | Proprietary | RPG item support |
| Vault API | GPL-3.0 | Economy abstraction |
| WorldGuard | GPL-3.0 | Region protection |

---

## 📊 Statistics

| Metric | Value |
|--------|-------|
| **Lines of Code** | ~2,500+ |
| **Java Files** | 15 |
| **Config Options** | 100+ |
| **Commands** | 5 |
| **Permissions** | 5 |
| **Java Version** | 21 |
| **Minecraft Version** | 1.21+ |

---

<div align="center">

**Enjoy repairing your items safely!** 🔨✨

Made with ❤️ by Syaaddd

[Report Bug](https://github.com/Syaaddd/OmniRepair/issues) · [Request Feature](https://github.com/Syaaddd/OmniRepair/issues) · [Download](https://github.com/Syaaddd/OmniRepair/releases)

</div>
