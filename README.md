# 🔨 OmniRepair

> **Repair Vanilla & RPG Items Safely. MMOItems Supported.**

<p align="center">
  <img src="https://img.shields.io/badge/version-1.0.1--SNAPSHOT-blue?style=flat-square" alt="Version">
  <img src="https://img.shields.io/badge/java-21+-orange?style=flat-square" alt="Java">
  <img src="https://img.shields.io/badge/platform-Paper%20%7C%20Spigot%20%7C%20Purpur-green?style=flat-square" alt="Platform">
  <img src="https://img.shields.io/badge/minecraft-1.21+-brightgreen?style=flat-square" alt="Minecraft">
  <img src="https://img.shields.io/badge/license-MIT-lightgrey?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/build-passing-success?style=flat-square" alt="Build">
</p>

---

## 📋 Daftar Isi

- [Arsitektur Sistem](#-arsitektur-sistem)
- [Struktur Kode](#-struktur-kode)
- [Features](#-features)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Plugin YAML](#-plugin-yaml)
- [Commands](#-commands)
- [Permissions](#-permissions)
- [Messages System](#-messages-system)
- [GUI Guide](#-gui-guide)
- [Repair System](#-repair-system)
- [Economy System](#-economy-system)
- [Integration Layer](#-integration-layer)
- [Utility Classes](#-utility-classes)
- [Safety & Protection](#-safety--protection)
- [Developer API](#-developer-api)
- [Building from Source](#-building-from-source)
- [Changelog v1.0.1](#-changelog-v101)
- [Troubleshooting](#-troubleshooting)
- [Roadmap](#-roadmap)
- [License](#-license)

---

## 🏗️ Arsitektur Sistem

OmniRepair menggunakan **arsitektur berlapis** dengan pola desain **Strategy Pattern** untuk penanganan repair, **Facade Pattern** untuk akses terpusat, dan **Soft Dependency** untuk semua integrasi plugin eksternal.

```
┌──────────────────────────────────────────────────────────────┐
│                    OmniRepair.java (Main)                     │  Entry point, dependency injection
├──────────────────────────────────────────────────────────────┤
│  Commands Layer                                               │
│  └── RepairCommand.java                                       │  /repair command dispatcher + tab completer
├──────────────────────────────────────────────────────────────┤
│  GUI Layer                                                    │
│  ├── GUIManager.java                                          │  GUI lifecycle management (open/close/track)
│  └── RepairGUI.java                                           │  GUI inventory construction (27-slot, 3 buttons)
├──────────────────────────────────────────────────────────────┤
│  Event Layer                                                  │
│  ├── GUIListener.java                                         │  InventoryClick/Drag/Close event handlers
│  └── RepairListener.java                                      │  Bulk repair orchestration + held item repair
├──────────────────────────────────────────────────────────────┤
│  Repair Strategy Layer (Strategy Pattern)                     │
│  ├── RepairHandler.java (abstract)                            │  Strategy interface + shared cost utilities
│  ├── VanillaRepair.java                                       │  Vanilla item repair via Damageable.setDamage(0)
│  └── MMOItemsRepair.java                                      │  MMOItems repair via fresh template + NBT sync
├──────────────────────────────────────────────────────────────┤
│  Economy Layer                                                │
│  └── EconomyHandler.java                                      │  Payment abstraction: Money / XP / Items / Free
├──────────────────────────────────────────────────────────────┤
│  Integration Layer (Soft Dependencies)                        │
│  ├── MMOItemsHook.java (634 lines)                            │  MMOItems API bridge + 4-layer fallback
│  ├── VaultHook.java (143 lines)                               │  Vault economy bridge
│  └── CustomEnchantHook.java (542 lines)                       │  AdvancedEnchantments bridge + NBT fallback
├──────────────────────────────────────────────────────────────┤
│  Utility Layer                                                │
│  ├── ItemUtils.java (373 lines)                               │  Item inspection: durability, blacklist, soulbound
│  ├── LoreUpdater.java (237 lines)                             │  Color translation (&, hex), lore sync, preview
│  └── NBTProtection.java (227 lines)                           │  Safe cloning, NBT verification, durability apply
└──────────────────────────────────────────────────────────────┘
```

### Pola Desain Utama

| Pola | Implementasi | Lokasi |
|------|-------------|--------|
| **Strategy Pattern** | `RepairHandler` abstract → `VanillaRepair` & `MMOItemsRepair` | `repair/` |
| **Facade Pattern** | `OmniRepair` main class menyediakan akses ke semua subsistem | `OmniRepair.java` |
| **Observer Pattern** | Bukkit event listeners untuk GUI events | `GUIListener.java` |
| **Dependency Injection** | Plugin instance di-inject ke semua class via constructor | Semua class |
| **Soft Dependency** | Semua integrasi graceful-degrade jika plugin tidak ada | `integration/` |

---

## 📁 Struktur Kode

```
src/main/java/com/github/Syaaddd/omniRepair/
├── OmniRepair.java                    # Main plugin class (351 lines)
├── commands/
│   └── RepairCommand.java             # Command executor & tab completer (238 lines)
├── economy/
│   └── EconomyHandler.java            # Economy abstraction (256 lines)
├── gui/
│   ├── GUIManager.java                # GUI lifecycle manager (94 lines)
│   └── RepairGUI.java                 # GUI inventory implementation (192 lines)
├── integration/
│   ├── CustomEnchantHook.java         # AdvancedEnchantments support (542 lines)
│   ├── MMOItemsHook.java              # MMOItems support (634 lines)
│   └── VaultHook.java                 # Vault economy support (143 lines)
├── listeners/
│   ├── GUIListener.java               # GUI click/close event handler (376 lines)
│   └── RepairListener.java            # Bulk repair logic (282 lines)
├── repair/
│   ├── RepairHandler.java             # Abstract repair base (92 lines)
│   ├── VanillaRepair.java             # Vanilla repair strategy (125 lines)
│   └── MMOItemsRepair.java            # MMOItems repair strategy (410 lines)
└── utils/
    ├── ItemUtils.java                 # Item inspection utilities (373 lines)
    ├── LoreUpdater.java               # Lore & color utilities (237 lines)
    └── NBTProtection.java             # NBT preservation & verification (227 lines)

src/main/resources/
├── plugin.yml                         # Bukkit plugin descriptor (37 lines)
├── config.yml                         # Main configuration (240 lines)
└── messages.yml                       # Message strings (139 lines)
```

**Total: 18 source files, ~4,634 lines of production code.**

---

## ✨ Features

### 🎮 Universal Durability Detection

Mendeteksi durabilitas dari berbagai tipe item dengan sistem prioritas:

1. **MMOItems** — Deteksi via official API (`MMOItems.getDurability()`), fallback ke NBT PersistentDataContainer (`mmoitems:durability`, `mmoitems:max_durability`, `mmoitems:current_durability`, `mmoitems:max_hp`)
2. **Vanilla Items** — Tools, armor, weapons, elytra, trident, shield via `Damageable.getDamage()`
3. **Automatic Lore Sync** — Update durability lore otomatis setelah repair (format: posisi ADD_BOTTOM / ADD_TOP / REPLACE_EXISTING)
4. **Fallback System** — Berjalan sempurna tanpa MMOItems

### 🖥️ Simplified Repair GUI

GUI 27-slot (3 baris) dengan hanya 3 tombol:

```
Row 1: [bg][bg][bg][bg][CLOSE][bg][bg][bg][bg]
Row 2: [bg][HAND][bg][ALL][bg][bg][bg][bg][bg]
Row 3: [bg][bg][bg][bg][bg][bg][bg][bg][bg]
```

| Slot | Posisi | Tombol | Fungsi | Material Default |
|------|--------|--------|--------|------------------|
| **10** | Row 2, Col 2 | **Repair Hand** | Repair item di tangan | `ANVIL` |
| **12** | Row 2, Col 4 | **Repair All** | Repair semua item di inventory | `HOPPER` |
| **4** | Row 1, Col 5 | **Close** | Tutup GUI | `BARRIER` |

- Background diisi `BLACK_STAINED_GLASS_PANE` (konfigurabel)
- Semua klik di GUI di-cancel (`setCancelled(true)`) untuk mencegah manipulasi item
- **GUI-Only System** — Semua repair harus melalui GUI. Tidak ada command `/repair hand` atau `/repair all`

### 💰 Economy & Cost System

Tiga metode pembayaran + free repair:

| # | Method | Config Key | Cara Kerja |
|---|--------|-----------|------------|
| 1 | **Vault Economy** | `economy.enabled: true` | Withdraw dari balance player via `Economy.withdrawPlayer()` |
| 2 | **XP Levels** | `economy.use-xp-cost: true` | Kurangi `player.setLevel(player.getLevel() - cost)` |
| 3 | **Item Cost** | `economy.use-item-cost: true` | Hapus item dari inventory slot per slot |
| 4 | **Free** | `omnirepair.free` permission | Bypass semua biaya |

### 🛡️ Safety & Protection

| Protection | Implementasi | File |
|------------|-------------|------|
| **NBT Preservation** | Clone + verify: enchantments, display name, lore, custom model data, PDC keys | `NBTProtection.java` |
| **Soulbound Check** | Scan lore untuk keyword soulbound dari config | `ItemUtils.java:323` |
| **Anvil Protection** | Blok MMOItems dari anvil enchant (cegah EXP leak) | `AnvilListener.java` |
| **Blacklist System** | 4 tier: material, lore-contains, name-contains, MMOItems IDs | `ItemUtils.java:266` |
| **Cost Cap** | `max-cost` dan `min-cost` mencegah eksploitasi | `RepairHandler.java:63` |
| **Rollback Safety** | Clone item SEBELUM repair; jika gagal, item asli tidak berubah | `VanillaRepair.java:77` |

### 🔌 Integrasi Plugin Eksternal

| Plugin | Status | Method | Fallback |
|--------|--------|--------|----------|
| **MMOItems** `6.9+` | ✅ Soft Dependency | Reflection API + NBT PDC | NBT-only |
| **Vault** `1.7+` | ✅ Soft Dependency | ServicesManager.getRegistration() | Free repair |
| **AdvancedEnchantments** | ✅ Soft Dependency | Reflection API + NBT PDC | NBT-only |
| **WorldGuard** `7.0+` | ❌ Config only | Belum diimplementasikan di kode | N/A |

### 🛡️ Anvil Protection (v1.0.1)

| Fitur | File | Deskripsi |
|-------|------|-----------|
| **PrepareAnvilEvent** | `AnvilListener.java` | Set result ke null jika item kiri adalah MMOItems |
| **InventoryClickEvent** | `AnvilListener.java` | Cancel klik result slot anvil untuk MMOItems |
| **Config Control** | `config.yml` | `anvil.block-mmoitems-enchant: true/false` |

---

## 📥 Installation

### Requirements

| Dependency | Version | Status | Keterangan |
|------------|---------|--------|------------|
| Paper / Spigot / Purpur | 1.21+ | ✅ Required | API 1.21 |
| Java | 21+ | ✅ Required | JDK 21+ |
| MMOItems | 6.9+ | ❌ Optional | Untuk RPG items |
| Vault | 1.7+ | ❌ Optional | Untuk economy |
| WorldGuard | 7.0+ | ❌ Optional | Untuk region protection (belum implemented) |

### Quick Install

```
1. Download  →  OmniRepair-1.0.0-SNAPSHOT.jar dari Releases
2. Copy      →  Taruh di server's plugins/ folder
3. Start     →  Jalankan server untuk generate config files
4. Configure →  Edit plugins/OmniRepair/config.yml & messages.yml
5. Reload    →  /repair reload (tanpa restart server)
```

### Load Order

> **PENTING:** MMOItems harus di-load sebelum OmniRepair. OmniRepair mendeteksi MMOItems saat `onEnable()` dan mengaktifkan integrasi. Jika MMOItems di-load setelahnya, restart server diperlukan.

---

## ⚙️ Configuration

### 1. `settings` — General Settings

```yaml
settings:
  debug: false                 # Debug mode: console logging detail
  support-mmoitems: true       # Aktifkan MMOItems API integration
  use-economy: true            # Gunakan Vault economy
  cost-per-percent: 10.0       # Biaya per 1% durability hilang
  max-cost: 5000.0             # Biaya maksimum per repair
  min-cost: 5.0                # Biaya minimum per repair
  max-bulk-repair: 360         # Max item untuk Repair All (360 = seluruh inventory)
```

### 2. `blacklist` — Item Blacklist

```yaml
blacklist:
  materials:                    # Block berdasarkan Material enum
    - BEDROCK
    - BARRIER
    - COMMAND_BLOCK
    - STRUCTURE_BLOCK
    - STRUCTURE_VOID
    - END_PORTAL_FRAME
    - ENCHANTED_GOLDEN_APPLE
    - TOTEM_OF_UNDYING

  lore-contains:                # Block berdasarkan lore (case-insensitive)
    - "&lEVENT ITEM"
    - "&cNO REPAIR"
    - "Unrepairable"
    - "[Event]"

  name-contains:                # Block berdasarkan display name (case-insensitive)
    - "Event Item"
    - "Admin Item"

  mmoitems-ids:                 # Block MMOItems berdasarkan ID
    - "EXAMPLE_SWORD_EVENT"
    - "EXAMPLE_ARMOR_BOSS"
```

### 3. `mmoitems` — MMOItems Integration

```yaml
mmoitems:
  enabled: true
  custom-cost-multiplier: 1.5     # Multiplier 1.5x untuk MMOItems

  sync-lore: true                 # Update lore durability display otomatis

  lore-format:                    # Format lore durability
    enabled: true
    line: "&7Durability: &a{current} &7/ &a{max} &7(&e{percent}%&7)"
    position: "ADD_BOTTOM"        # ADD_BOTTOM | ADD_TOP | REPLACE_EXISTING

  lore-patterns:                  # Regex pattern untuk durability lines (REPLACE_EXISTING)
    - ".*Durability:.*"
    - ".*Durability.*"
    - ".*HP:.*"
```

### 4. `economy` — Economy Options

```yaml
economy:
  enabled: true                    # Gunakan Vault economy

  # Metode 1: Item Cost
  use-item-cost: false
  cost-item:
    material: "IRON_INGOT"
    amount-per-percent: 0.01       # 0.01 item per 1% durability

  # Metode 2: XP Cost
  use-xp-cost: false
  xp-per-percent: 0.5              # 0.5 XP level per 1% durability

  # Free repair permission
  free-repair-permission: "omnirepair.free"
```

> **Priority:** Item Cost > XP Cost > Money > Free.
> Jika `use-xp-cost: true` dan `use-item-cost: true`, item cost diprioritaskan.

### 5. `safety` — Safety Settings

```yaml
safety:
  respect-soulbound: true
  soulbound-lore:                  # Lore patterns untuk deteksi soulbound
    - "Soulbound"
    - "Soul Bound"
    - "&9Soulbound"

  preserve-nbt: true               # Selalu true (tidak bisa dimatikan)
  check-balance: true              # Cek balance sebelum repair
  max-durability-restore: 0        # 0 = unlimited
```

### 6. `gui` — GUI Customization

```yaml
gui:
  title: "&8&l🔨 RPG Mender"
  size: 27                         # 3 baris (multiple of 9)

  background: "BLACK_STAINED_GLASS_PANE"
  background-name: " "

  slots:                           # Posisi tombol (customizable)
    repair-hand: 10
    repair-all: 12
    close: 4

  buttons:                         # Material tombol
    repair-hand: "ANVIL"
    repair-all: "HOPPER"
    close: "BARRIER"

  button-names:                    # Display name & lore
    repair-hand: "&a&lRepair Hand"
    repair-hand-lore:
      - ""
      - "&7Click to repair the item in your hand."
      - "&eCost: Based on damage"

    repair-all: "&e&lRepair All Inventory"
    repair-all-lore:
      - ""
      - "&7Click to repair all damaged items."
      - "&eRequires permission: omnirepair.bulk"

    close: "&c&lClose"
    close-lore:
      - "&7Close this GUI"
```

### 7. `effects` — Visual Effects

```yaml
effects:
  sound:
    enabled: true
    type: "BLOCK_ANVIL_USE"       # Nama sound (legacy atau namespaced key)
    volume: 1.0
    pitch: 1.0

  particles:
    enabled: true
    type: "VILLAGER_HAPPY"        # Particle type enum
    count: 10
    offset-x: 0.5
    offset-y: 0.5
    offset-z: 0.5
    speed: 0.5

  action-bar:
    enabled: true
    message: "&a✓ Item Repaired! Cost: ${cost}"
```

### 8. `anvil` — Anvil Protection (v1.0.1)

```yaml
anvil:
  block-mmoitems-enchant: true     # Blok MMOItems dari anvil enchant
```

Melindungi player dari kehilangan EXP secara tidak sengaja saat memasukkan MMOItems + enchantment book ke anvil vanilla.

### 9. `worldguard` — WorldGuard Integration

```yaml
worldguard:
  enabled: true
  disabled-regions:               # Region dimana repair diblok
    - "repair-disabled-zone"
    - "pvp-arena"
  allowed-regions: []              # Whitelist region (kosong = semua diizinkan)
```

> ⚠️ **Note:** WorldGuard integration terdaftar di config dan `plugin.yml` sebagai softdepend, namun **belum diimplementasikan di kode**. Region check tidak aktif.

### 10. `update-checker`

```yaml
update-checker:
  enabled: true
  notify-on-join: true
```

> ⚠️ **Note:** Update checker terdaftar di config namun **belum diimplementasikan di kode**.

---

## 📄 Plugin YAML

**File:** `src/main/resources/plugin.yml`

```yaml
name: OmniRepair
version: '1.0.0-SNAPSHOT'
main: com.github.Syaaddd.omniRepair.OmniRepair
api-version: '1.21'
authors: [Syaaddd]
description: Repair Vanilla & RPG Items Safely. MMOItems Supported.
website: https://github.com/Syaaddd/OmniRepair

softdepend:
  - MMOItems
  - Vault
  - WorldGuard

commands:
  repair:
    description: Open the Repair GUI
    usage: /<command> [hand|all]
    aliases: [omnirepair, fix]
    permission: omnirepair.use
```

### Command & Permission Tree

```
/repair
├── (no args)           → buka GUI sendiri        [omnirepair.use: true]
├── <player>            → buka GUI untuk player   [omnirepair.admin: op]
├── reload              → reload config           [omnirepair.admin: op]
├── debug               → toggle debug mode       [omnirepair.admin: op]
└── help                → tampilkan help          [omnirepair.use: true]

Aliases: /omnirepair, /fix
```

### Permission Children

```yaml
omnirepair.admin:
  description: Admin commands
  default: op
  children:
    omnirepair.use: true    # Inherit use permission
    omnirepair.bulk: true   # Inherit bulk permission
    omnirepair.hand: true   # Inherit hand permission (unused)
```

---

## 💬 Commands

### Player Commands

| Command | Description | Permission | Implementasi |
|---------|-------------|------------|--------------|
| `/repair` | Open repair GUI | `omnirepair.use` | `RepairCommand.java:76` |
| `/repair help` | Show help | `omnirepair.use` | `RepairCommand.java:154` |

### Admin/Console Commands

| Command | Description | Permission | Implementasi |
|---------|-------------|------------|--------------|
| `/repair <player>` | Open GUI for player | `omnirepair.admin` | `RepairCommand.java:94` |
| `/repair reload` | Reload config + messages | `omnirepair.admin` | `RepairCommand.java:135` |
| `/repair debug` | Toggle debug mode | `omnirepair.admin` | `RepairCommand.java:173` |
| `/repair help` | Show help (admin view) | `omnirepair.admin` | `RepairCommand.java:154` |

### Tab Completion

`RepairCommand.onTabComplete()` memberikan saran:
- `help` — selalu tersedia
- `reload`, `debug` — hanya jika `omnirepair.admin`
- Nama player online dengan `omnirepair.use` — untuk console/admin

### Message System

Semua pesan menggunakan format `prefix + path` dari `messages.yml`:
```yaml
prefix: "&8[&6OmniRepair&8] "
```

`RepairCommand.sendMessage()` otomatis menambahkan prefix dan colorize.

---

## 🔐 Permissions

| Permission | Default | Deskripsi | Digunakan di |
|------------|---------|-----------|-------------|
| `omnirepair.use` | `true` | Buka GUI repair | `RepairCommand.java:82`, `GUIListener.java:212` |
| `omnirepair.bulk` | `false` | Gunakan Repair All | `GUIListener.java:212` |
| `omnirepair.hand` | `true` | (Unused — reserved) | `plugin.yml` only |
| `omnirepair.admin` | `op` | Admin commands (reload, debug, target player) | `RepairCommand.java:136,174` |
| `omnirepair.free` | `false` | Repair gratis (bypass semua biaya) | `EconomyHandler.java:36,105` |

### Permission Hierarchy

```
omnirepair.admin (op)
├── omnirepair.use (true)     → inherit
├── omnirepair.bulk (false)   → inherit
└── omnirepair.hand (true)    → inherit (unused)
```

---

## 💬 Messages System

**File:** `src/main/resources/messages.yml` (139 lines, fully customizable)

Semua pesan plugin disimpan di `messages.yml` dan di-load via `OmniRepair.saveMessagesConfig()` dan `loadMessages()`.

### Metode Akses

```java
// Di OmniRepair.java
public String getMessage(String path) {
    String message = messagesConfig.getString(path, "");
    return loreUpdater.colorize(message);
}
```

### Sections

| Section | Path Prefix | Jumlah Pesan | Contoh |
|---------|------------|-------------|--------|
| **General** | `general.*` | 10 | `reload-success`, `no-permission`, `player-not-found` |
| **Repair** | `repair.*` | 11 | `success`, `not-damaged`, `blacklisted`, `soulbound` |
| **GUI** | `gui.*` | 7 | Button names, lore, error messages |
| **Admin** | `admin.*` | 5 | Update checker, debug, WorldGuard |
| **Action Bar** | `action-bar.*` | 5 | Short temporary messages |
| **Boss Bar** | `boss-bar.*` | 3 | Bulk repair progress |

### Placeholder Support

Semua pesan mendukung placeholder `{variable}` yang di-replace saat runtime:

| Placeholder | Contoh Pesan | Diganti di |
|------------|-------------|-----------|
| `{cost}` | `"Cost: ${cost}"` | `GUIListener.java:284` |
| `{player}` | `"Player {player} not found"` | `RepairCommand.java:103` |
| `{amount}` | `"Repaired {amount} items"` | `RepairListener.java:127` |
| `{needed}` | `"Need ${needed}"` | `GUIListener.java:133` |
| `{balance}` | `"have ${balance}"` | `GUIListener.java:134` |
| `{current}` | `"{current} / {max}"` | `LoreUpdater.java:93` |

### Color Code Support

```java
// LoreUpdater.colorize() — dual format support
public String colorize(String text) {
    // Step 1: Legacy & codes → § codes via ChatColor.translateAlternateColorCodes()
    // Step 2: Hex &#RRGGBB → §R§R§G§G§B§B via regex Pattern.compile("&#([A-Fa-f0-9]{6})")
}
```

---

## 🎨 GUI Guide

### Lifecycle Management — `GUIManager.java`

```java
public class GUIManager {
    private final Map<UUID, RepairGUI> activeGUIs = new HashMap<>();
    
    openGUI(Player)   → closeGUI() + new RepairGUI() + activeGUIs.put() + gui.open()
    closeGUI(Player)  → activeGUIs.remove() + player.closeInventory()
    getGUI(Player)    → activeGUIs.get(uuid)
    hasGUI(Player)    → activeGUIs.containsKey(uuid)
    removeGUI(Player) → activeGUIs.remove() (tanpa close inventory)
    closeAll()        → Iterasi semua + close + clear (untuk shutdown)
}
```

### GUI Construction — `RepairGUI.java`

Mengimplementasikan `InventoryHolder` untuk membuat inventory 27-slot:

```java
public RepairGUI(OmniRepair plugin, Player player) {
    // 1. Baca slot dari config (repair-hand: 10, repair-all: 12, close: 4)
    // 2. Buat inventory: Bukkit.createInventory(this, size, title)
    // 3. Panggil initializeGUI()
    //     → fillBackground() — isi semua non-button slot dengan glass pane
    //     → placeButtons() — buat 3 button items dengan material/name/lore dari config
}
```

### Event Handling — `GUIListener.java`

```java
@EventHandler(priority = EventPriority.HIGH)
onInventoryClick(InventoryClickEvent) {
    // 1. Verifikasi player dan GUI aktif
    // 2. Cancel semua event (setCancelled(true))
    // 3. Route ke handler berdasarkan slot:
    //    - repairHandSlot  → handleRepairHandClick()
    //    - repairAllSlot   → handleRepairAllClick()
    //    - closeSlot       → closeInventory() + clickSound
}
```

#### Repair Hand Workflow (`handleRepairHandClick`)

```
Player klik "Repair Hand"
  → Dapat item dari main hand (fallback ke offhand)
  → ItemUtils.canRepair() [null/air check + blacklist + soulbound + isDamaged]
  → Tentukan tipe item: MMOItemsHook.isMMOItem()? → MMOItemsRepair : VanillaRepair
  → RepairHandler.getRepairCost() [damage% × cost-per-percent × multiplier]
  → EconomyHandler.canAfford() [money/XP/items check + free permission]
  → RepairHandler.repair() [clone → copy custom enchants → repair → verify]
  → EconomyHandler.withdraw() [withdraw payment]
  → Set repaired item ke tangan yang benar (main/offhand)
  → playSuccessEffects() [sound + particles + action bar]
```

#### Repair All Workflow (`handleRepairAllClick`)

```
Player klik "Repair All"
  → Cek permission omnirepair.bulk
  → Cek settings.bulk-repair enabled
  → Close GUI
  → RepairListener.performBulkRepair()
    → Scan inventory (0-35, 36-40 armor, offhand)
    → Filter: item != null && !AIR && canRepair()
    → Cek max-bulk-repair limit (default 360)
    → Calculate total cost (sum semua item)
    → EconomyHandler.canAfford()
    → Loop: repair setiap item via handler yang sesuai
      → Preserve stack size (setAmount)
      → Set kembali ke slot original
    → EconomyHandler.withdraw(totalCost)
    → playSuccessEffects()
```

---

## 🔧 Repair System

### Strategy Pattern — `RepairHandler` (Abstract)

```java
public abstract class RepairHandler {
    // Abstract methods (harus diimplement)
    public abstract boolean canRepair(ItemStack item);
    public abstract double getRepairCost(ItemStack item);
    public abstract ItemStack repair(ItemStack item, Player player);

    // Shared utilities
    protected boolean exceedsMaxCost(double cost);      // > settings.max-cost?
    protected double applyMMOCostMultiplier(double);    // × mmoitems.custom-cost-multiplier
    protected double getMinCost();                       // settings.min-cost
    protected double getCostPerPercent();                // settings.cost-per-percent
}
```

### VanillaRepair.java

```java
canRepair() → false jika: null/air, MMOItem, bukan durability item, atau tidak rusak

getRepairCost() → damagePercent × costPerPercent, clamp(minCost, maxCost)

repair(item, player):
  1. Clone item (nbtProtection.cloneSafely) untuk rollback safety
  2. Copy custom enchantments dari AdvancedEnchantments (CustomEnchantHook)
  3. Set Damageable.setDamage(0) — full repair
  4. Verify NBT preservation (debug mode)
  5. Return repaired item
```

**Vanilla Durability Table** (dari `ItemUtils.getMaxVanillaDurability()`):

| Material Group | Items | Durability |
|---------------|-------|-----------|
| Wooden Tools | SWORD, PICKAXE, AXE, SHOVEL, HOE | 60 |
| Golden Tools | SWORD, PICKAXE, AXE, SHOVEL, HOE | 33 |
| Stone Tools | SWORD, PICKAXE, AXE, SHOVEL, HOE | 132 |
| Iron Tools | SWORD, PICKAXE, AXE, SHOVEL, HOE | 251 |
| Diamond Tools | SWORD, PICKAXE, AXE, SHOVEL, HOE | 1562 |
| Netherite Tools | SWORD, PICKAXE, AXE, SHOVEL, HOE | 2032 |
| Bow / Fishing Rod | — | 385 |
| Crossbow / Trident | — | 251 |
| Shield | — | 337 |
| Leather Armor | HELMET/CHESTPLATE/LEGGINGS/BOOTS | 56/81/76/66 |
| Chainmail Armor | HELMET/CHESTPLATE/LEGGINGS/BOOTS | 166/241/226/199 |
| Iron Armor | HELMET/CHESTPLATE/LEGGINGS/BOOTS | 166/241/226/199 |
| Golden Armor | HELMET/CHESTPLATE/LEGGINGS/BOOTS | 78/113/106/92 |
| Diamond Armor | HELMET/CHESTPLATE/LEGGINGS/BOOTS | 364/529/496/430 |
| Netherite Armor | HELMET/CHESTPLATE/LEGGINGS/BOOTS | 408/593/556/482 |
| Turtle Helmet | — | 276 |
| Elytra | — | 432 |
| Shears | — | 239 |
| Flint & Steel | — | 65 |
| Carrot on a Stick | — | 26 |
| Warped Fungus on a Stick | — | 100 |

### MMOItemsRepair.java

```java
canRepair() → false jika: null/air, MMOItemsHook disabled, bukan MMOItem,
               MMOItems ID blacklisted, template item null,
               item tidak punya DURABILITY stat (fallback ke NBT check)

getRepairCost() → damagePercent × costPerPercent × mmoMultiplier, clamp(minCost, maxCost)

repair(item, player):
  1. Dapatkan Type dan ID dari MMOItems API
  2. Dapatkan fresh template: MMOItems.plugin.getItem(type, id)
  3. Clone template
  4. Copy custom enchantments via CustomEnchantHook.copyCustomEnchantments()
  5. Copy vanilla enchantments dari original item
  6. Copy display name jika original punya custom name
  7. Merge lore:
     a. Mulai dengan template lore (fresh durability display)
     b. Tambahkan lore original yang BUKAN durability-related
     c. Gunakan regex patterns dari config untuk identifikasi durability lines
  8. Copy item flags
  9. setMaxDurability() — 4-layer fallback:
     Layer 1: Baca max durability dari item's PDC
     Layer 2: Baca dari fresh template
     Layer 3: Baca langsung dari PDC keys (mmoitems:max_durability)
     Layer 4: Default 100.0 (known bug — hardcoded fallback)
  10. Set durability PDC keys: durability, max_durability, current_durability
  11. Sync lore: updateDurabilityLore() dengan format config
  12. Return repaired item
```

### Cost Calculation Formula

```
Base Cost    = damagePercent × costPerPercent
MMO Cost     = Base Cost × mmoitems.custom-cost-multiplier
Final Cost   = clamp(MMO Cost, minCost, maxCost)

dimana:
  damagePercent = (1 - currentDurability / maxDurability) × 100
  costPerPercent = settings.cost-per-percent (default: 10.0)
```

### Contoh Perhitungan

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

---

## 💰 Economy System

### EconomyHandler.java (256 lines)

Three payment methods + free repair, diatur via config:

```java
public enum PaymentMethod {
    MONEY,    // Vault economy
    XP,       // Experience levels
    ITEMS,    // Material items
    FREE      // Gratis (omnirepair.free permission)
}
```

### Payment Method Priority

```
Item Cost (use-item-cost: true)
  → XP Cost (use-xp-cost: true)
    → Money (economy.enabled: true)
      → Free (omnirepair.free permission or all disabled)
```

### canAfford() Flow

```java
canAfford(player, cost):
  1. Jika cost <= 0 → return true (gratis)
  2. Jika player hasPermission("omnirepair.free") → return true
  3. Route ke method sesuai payment method:
     a. canAffordItems(player, cost)  → hitung item di inventory
     b. canAffordXP(player, cost)     → player.getLevel() >= ceil(cost)
     c. canAffordMoney(player, cost)  → VaultHook.hasEnough()
```

### withdraw() Flow

```java
withdraw(player, cost):
  1. Jika cost <= 0 → return true
  2. Jika player hasPermission("omnirepair.free") → return true
  3. Route ke method sesuai payment method:
     a. withdrawItems(player, cost)  → scan inventory, kurangi amount per slot
     b. withdrawXP(player, cost)     → player.setLevel(level - cost)
     c. withdrawMoney(player, cost)  → VaultHook.withdraw()
```

### Item Cost Detail

```yaml
economy:
  use-item-cost: true
  cost-item:
    material: "IRON_INGOT"
    amount-per-percent: 0.01
```

Perhitungan jumlah item yang dibutuhkan:
```java
int requiredAmount = (int) Math.ceil(cost * amountPerPercent * 100);
// cost = damagePercent × costPerPercent
// Contoh: 80% damage × $10 = $800 cost
// required = ceil(800 × 0.01 × 100) = ceil(800) = 800 Iron Ingot
```

### VaultHook.java (143 lines)

```java
isEnabled()  → config check + economy != null + use-economy
hasEnough()  → economy.has(player, amount) [atau true jika free permission]
withdraw()   → economy.withdrawPlayer(player, amount)
getBalance() → economy.getBalance(player) [atau Double.MAX_VALUE jika disabled]
format()     → economy.format(amount) [atau "$X.XX" fallback]
```

---

## 🔌 Integration Layer

### MMOItemsHook.java (634 lines)

Integrasi soft-dependency dengan MMOItems menggunakan reflection + NBT fallback.

#### Inisialisasi

```java
public MMOItemsHook(OmniRepair plugin) {
    // 1. Cek plugin "MMOItems" via Bukkit.getPluginManager()
    // 2. Cek config mmoitems.enabled
    // 3. initializeReflection() — cari method getDurability, getMaxDurability, setDurability
    // 4. initializeNBTKeys() — buat NamespacedKey("mmoitems", "durability") dan max_durability
}
```

#### Reflection API

```java
// Mencari method dengan parameter (ItemStack) dan return Double/Integer
private void initializeReflection() {
    for (Method method : MMOItems.class.getDeclaredMethods()) {
        if (method.getName() == "getDurability" && params == 1)
            getDurabilityMethod = method;
        if (method.getName() == "getMaxDurability" && params == 1)
            getMaxDurabilityMethod = method;
        if (method.getName() == "setDurability" && params == 2)
            setDurabilityMethod = method;
    }
}
```

#### isDamaged() — 4 Method Fallback

```java
Method 1: API comparison  → getDurability() < getMaxDurability()
Method 2: NBT PDC reading → readDurabilityFromNBT() < readMaxDurabilityFromNBT()
Method 3: PDC key scan    → cari key mengandung "durability"/"hp"/"health" lalu baca value
Method 4: Stat checking    → MMOItem.hasData(ItemStats.DURABILITY)
```

#### NBT Keys yang Didukung

| Key | Tipe | Deskripsi |
|-----|------|-----------|
| `mmoitems:durability` | DOUBLE / INTEGER | Current durability |
| `mmoitems:current_durability` | DOUBLE | Alternative current durability key |
| `mmoitems:max_durability` | DOUBLE / INTEGER | Max durability |
| `mmoitems:max_hp` | DOUBLE | Alternative max durability key |

#### isMMOItem() Detection

```java
public boolean isMMOItem(ItemStack item) {
    String id = MMOItems.getID(item);
    return id != null && !id.isEmpty();  // Vanilla items return empty string
}
```

### CustomEnchantHook.java (542 lines)

Integrasi dengan AdvancedEnchantments untuk preservasi custom enchantments saat repair.

#### 4 Known API Class Paths

```java
String[] possibleAPIClasses = {
    "com.bgsoftware.advancedenchantments.api.AdvancedEnchantmentsAPI",
    "com.bgsoftware.advancedenchantments.api.EnchantmentAPI",
    "com.advancedenchantments.api.AdvancedEnchantmentsAPI",
    "org.phoenixframework.advancedenchantments.api.AdvancedEnchantmentsAPI"
};
```

#### 8 Known NBT Keys

```java
possibleNBTKeys = {
    "advancedenchantments", "advanced_enchantments", "ae_enchantments",
    "ae", "custom_enchants", "custom_enchantments", "enchants", "enchantment"
};
```

#### copyCustomEnchantments() — PDC Key Copy

```java
public boolean copyCustomEnchantments(ItemStack source, ItemStack target) {
    // Untuk setiap PDC key di source:
    //   Jika key mengandung "enchant" atau namespace "advancedenchant"/"ae":
    //     Copy berdasarkan tipe data: STRING, INTEGER, DOUBLE, BYTE_ARRAY
    // Set target meta
    // Return true jika ada yang di-copy
}
```

### VaultHook.java (143 lines)

```java
public VaultHook(OmniRepair plugin) {
    // 1. Cek plugin "Vault" via Bukkit.getPluginManager()
    // 2. Jika ada: ServicesManager.getRegistration(Economy.class).getProvider()
    // 3. Jika tidak: economy = null (semua repair gratis)
}
```

---

## 🔧 Utility Classes

### ItemUtils.java (373 lines)

Utility untuk inspeksi item: durability, blacklist, soulbound.

#### isDamaged() — Dual Detection

```java
public boolean isDamaged(ItemStack item) {
    // Priority 1: MMOItems — jika MMOItem, gunakan MMOItemsHook.isDamaged()
    // Priority 2: Vanilla — hasVanillaDamage() via Damageable interface
}
```

#### hasVanillaDamage()

```java
public boolean hasVanillaDamage(ItemStack item) {
    // 1. isDurabilityItem(item.getType()) → maxDurability > 0
    // 2. ItemMeta instanceof Damageable
    // 3. damageable.hasDamage() && damage > 0 && damage <= maxDurability
}
```

#### Blacklist — 4 Tier Check

```java
public boolean isBlacklisted(ItemStack item) {
    // Tier 1: Material — item.getType() in config list
    // Tier 2: Lore — scan lore lines, case-insensitive contains check
    // Tier 3: Name — display name, case-insensitive contains check
    // Tier 4: MMOItems ID — MMOItems.getID() in config list
}
```

#### Soulbound — Lore Scan

```java
public boolean isSoulbound(ItemStack item) {
    // Jika safety.respect-soulbound: true
    // Scan lore untuk string dari safety.soulbound-lore config
    // Case-insensitive contains check per line
}
```

#### Max Vanilla Durability Table

Switch expression dengan 45+ material mapping (lihat tabel di atas).

### LoreUpdater.java (237 lines)

#### colorize() — Dual Format Color Translation

```java
public String colorize(String text) {
    // Step 1: ChatColor.translateAlternateColorCodes('&', text)
    //   → &a → §a, &c → §c, dll.
    // Step 2: Regex replace &#RRGGBB → §R§R§G§G§B§B
    //   → Pattern.compile("&#([A-Fa-f0-9]{6})")
    //   → Setiap hex char dipisah dengan § prefix
}
```

#### updateDurabilityLore() — MMOItems Lore Sync

```java
public ItemStack updateDurabilityLore(ItemStack item, double current, double max) {
    // 1. Baca format dari config: "&7Durability: &a{current} &7/ &a{max}..."
    // 2. Replace placeholder: {current}, {max}, {percent}
    // 3. Handle position:
    //    ADD_BOTTOM       → lore.add(coloredLine)
    //    ADD_TOP          → lore.add(0, coloredLine)
    //    REPLACE_EXISTING → remove if matches patterns, lalu add
    // 4. Set meta lore dan return
}
```

#### formatDurability() — Color-coded Display

```java
> 50%  → GREEN
> 25%  → YELLOW
≤ 25%  → RED
```

### NBTProtection.java (227 lines)

#### cloneSafely() — Safe Item Cloning

```java
public ItemStack cloneSafely(ItemStack item) {
    try {
        return item.clone();  // Preserve semua NBT via Bukkit
    } catch (Exception e) {
        return new ItemStack(item.getType(), item.getAmount());  // Fallback minimal
    }
}
```

#### verifyNBT() — Comprehensive Verification

Memeriksa 8 aspek NBT preservation:

```java
public boolean verifyNBT(ItemStack original, ItemStack modified) {
    // 1. Tipe item → original.getType() == modified.getType()
    // 2. Amount    → original.getAmount() == modified.getAmount()
    // 3. ItemMeta  → original.hasItemMeta() == modified.hasItemMeta()
    // 4. Enchantments → count + setiap enchant level
    // 5. Display name → content equality
    // 6. Lore     → presence (content change = warning only)
    // 7. Custom Model Data → value equality
    // 8. PDC Keys → setiap key dari original ada di modified (STRING/INTEGER/DOUBLE/LONG/BYTE_ARRAY)
    // 9. Custom Enchantments → hasCustomEnchantments() check via CustomEnchantHook
}
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

OmniRepair plugin = (OmniRepair) Bukkit.getPluginManager().getPlugin("OmniRepair");
// atau via JavaPlugin.getPlugin():
OmniRepair plugin = JavaPlugin.getPlugin(OmniRepair.class);
```

### API Class Overview

| Method | Return | Description |
|--------|--------|-------------|
| `getItemUtils()` | `ItemUtils` | Item inspection utilities |
| `getNBTProtection()` | `NBTProtection` | NBT preservation & cloning |
| `getLoreUpdater()` | `LoreUpdater` | Color translation & lore sync |
| `getVanillaRepair()` | `VanillaRepair` | Vanilla item repair handler |
| `getMmoItemsRepair()` | `MMOItemsRepair` | MMOItems item repair handler |
| `getEconomyHandler()` | `EconomyHandler` | Economy abstraction layer |
| `getGuiManager()` | `GUIManager` | GUI lifecycle manager |
| `getMmoItemsHook()` | `MMOItemsHook` | MMOItems integration hook |
| `getVaultHook()` | `VaultHook` | Vault economy hook |
| `getCustomEnchantHook()` | `CustomEnchantHook` | AdvancedEnchantments hook |
| `getMessages()` | `FileConfiguration` | Messages configuration |
| `getMessage(path)` | `String` | Colorized message by path |
| `colorize(text)` | `String` | Color code translation |

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

// Repair tanpa player (silent — untuk bulk repair)
ItemStack repaired = plugin.getVanillaRepair().repair(item);

// Repair MMOItems
ItemStack repaired = plugin.getMmoItemsRepair().repair(item, player);
```

### Economy Integration

```java
import com.github.Syaaddd.omniRepair.economy.EconomyHandler;

EconomyHandler economy = plugin.getEconomyHandler();

PaymentMethod method = economy.getPaymentMethod();  // MONEY | XP | ITEMS | FREE
boolean canAfford    = economy.canAfford(player, cost);
boolean success      = economy.withdraw(player, cost);
String costDisplay   = economy.getCostString(cost);  // "$10.00" | "5 XP Levels" | "10x iron ingot"
```

### MMOItems Hook

```java
MMOItemsHook mmoHook = plugin.getMmoItemsHook();

boolean isMMOItem = mmoHook.isMMOItem(item);
boolean isDamaged = mmoHook.isDamaged(item);
double current    = mmoHook.getDurability(item);
double max        = mmoHook.getMaxDurability(item);
double percent    = mmoHook.getDurabilityPercent(item);
boolean blacklist = mmoHook.isBlacklisted(mmoItemId);
```

### Contoh: Custom Repair Command

```java
@Command("customrepair")
public void onCustomRepair(Player player) {
    ItemStack item = player.getInventory().getItemInMainHand();

    if (!plugin.getItemUtils().canRepair(item)) {
        player.sendMessage("Item tidak bisa direpair!");
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
        player.sendMessage("Item berhasil direpair! Biaya: " +
            plugin.getEconomyHandler().getCostString(cost));
    }
}
```

### Contoh: Bulk Repair Custom

```java
public void customBulkRepair(Player player) {
    // Akses RepairListener untuk bulk repair logic
    plugin.getRepairListener().performBulkRepair(player);

    // Atau buka GUI untuk player
    plugin.getGuiManager().openGUI(player);
}
```

### NBT Protection

```java
NBTProtection nbt = plugin.getNBTProtection();

// Clone item dengan safety
ItemStack backup = nbt.cloneSafely(item);

// Verifikasi NBT preservation
boolean preserved = nbt.verifyNBT(original, modified);

// Apply durability only (tanpa mengubah NBT lain)
ItemStack result = nbt.applyDurabilityOnly(target, source);
```

---

## 🏗️ Building from Source

### Prerequisites

- Java JDK 21+
- Maven 3.8+ atau IntelliJ IDEA
- Git

### Build via Maven CLI

```bash
git clone https://github.com/Syaaddd/OmniRepair.git
cd OmniRepair
mvn clean package
# Output: target/OmniRepair-1.0.0-SNAPSHOT.jar
```

### Build via IntelliJ IDEA

1. Buka project di IntelliJ IDEA
2. Tunggu Maven selesai import dependencies
3. **Build → Build Project** (`Ctrl+F9`)
4. JAR tersedia di `target/OmniRepair-1.0.0-SNAPSHOT.jar`

### Build Scripts

| File | Deskripsi |
|------|-----------|
| `build.ps1` | PowerShell build script |
| `compile.ps1` | PowerShell compile script |
| `quick-build.bat` | Quick build batch file |
| `mvnw.cmd` | Maven wrapper (Windows) |

### Dependencies (pom.xml)

| Library | Version | Scope | Type |
|---------|---------|-------|------|
| Paper API | 1.21.11-R0.1-SNAPSHOT | provided | Required |
| MythicLib-dist | 1.6.2-SNAPSHOT | provided | Optional (MMOItems dep) |
| MMOItems-API | 6.9.5-SNAPSHOT | provided | Soft dependency |
| VaultAPI | 1.7.1 | provided | Soft dependency |
| WorldGuard-Bukkit | 7.0.14 | provided | Soft dependency |

---

## 📝 Changelog v1.0.1

### Bug #1 — False Negative Repair untuk Mace & Elytra

**Root Cause:** `ItemUtils.getMaxVanillaDurability()` tidak memiliki entry untuk `Material.MACE` (item baru Minecraft 1.21), sehingga `isDamaged()` selalu mengembalikan `false`. Elytra sudah ada namun deteksi diperkuat.

**Perbaikan:**
| File | Perubahan |
|------|-----------|
| `ItemUtils.java:177` | Tambah `case MACE -> 500` ke switch durability |
| `ItemUtils.java:27` | Tambah method `isDamageable()` untuk deteksi item non-durability |
| `MMOItemsHook.java:462` | Method 3 sekarang fall-through ke Method 4 (tidak langsung return true) |
| `MMOItemsRepair.java:80` | Tambah pengecekan `isDamaged()` di `canRepair()` |
| `GUIListener.java:105` | Tambah pengecekan `isDamageable()` dengan pesan spesifik |
| `messages.yml` | Tambah pesan `repair.not-damageable` |

**Acceptance Criteria:**
- ✅ Mace rusak → GUI terbuka, repair berhasil
- ✅ Mace full HP → "Item tidak rusak"
- ✅ Elytra rusak → GUI terbuka, repair berhasil
- ✅ Elytra full HP → "Item tidak rusak"
- ✅ Item non-durability (misal: stick) → "Item ini tidak memiliki durability"

### Bug #2 — EXP Leak saat MMOItems di Anvil

**Root Cause:** Vanilla anvil tidak mendeteksi MMOItems sebagai item khusus, sehingga EXP player tetap terpotong saat mencoba enchant.

**Perbaikan:**
| File | Perubahan |
|------|-----------|
| `listeners/AnvilListener.java` | **Baru** — listener untuk `PrepareAnvilEvent` + `InventoryClickEvent` |
| `OmniRepair.java:167` | Register `AnvilListener` di `initializeListeners()` |
| `config.yml` | Tambah section `anvil.block-mmoitems-enchant: true` |
| `messages.yml` | Tambah pesan `repair.anvil-blocked` |

**Cara Kerja AnvilListener:**
```
1. Player masukkan MMOItems + enchantment book ke anvil
2. PrepareAnvilEvent fires at HIGHEST priority
3. Jika item slot-0 adalah MMOItems → event.setResult(null)
4. Player klik result slot (slot 2)
5. InventoryClickEvent fires at HIGHEST priority
6. Cancel event + kirim notifikasi ke player
7. EXP player tidak berkurang!
```

**Acceptance Criteria:**
- ✅ Frostmaw + enchant book → EXP tidak berkurang, notifikasi muncul
- ✅ Frostmaw Spear + enchant book → EXP tidak berkurang, notifikasi muncul
- ✅ Diamond Sword + enchant book → Enchant normal (tidak diblok)
- ✅ Config `anvil.block-mmoitems-enchant: false` → anvil tidak diblok
- ✅ `/repair reload` setelah ubah config → langsung生效

---

### Plugin Tidak Mau Load

**Error:** `Could not load plugin`
- Pastikan Java 21+ terinstall
- Gunakan Paper/Spigot/Purpur 1.21+
- Cek console untuk pesan error lengkap

### MMOItems Items Tidak Bisa Direpair

**Error:** Item tampak "tidak rusak" atau repair gagal
```yaml
# Verifikasi urutan load: MMOItems harus load SEBELUM OmniRepair
# Aktifkan debug mode untuk detail:
settings:
  debug: true

# Cek console untuk:
# "✓ MMOItems integration enabled"
# "[DEBUG] MMOItem Check - ID: xxx, Current: y, Max: z"
```

### Economy Tidak Berfungsi

**Error:** "Insufficient funds" atau repair selalu gratis
```yaml
# Pastikan Vault terinstall dan ada economy plugin (Essentials, CMI, dll)
economy:
  enabled: true
settings:
  use-economy: true
```

### NBT / Enchantment Hilang

> ⚠️ Ini **seharusnya tidak terjadi**. `NBTProtection.verifyNBT()` memeriksa 8 aspek NBT.
```yaml
settings:
  debug: true
# Cek console untuk "[DEBUG] NBT verification passed/failed"
# Laporkan ke GitHub Issues jika terjadi
```

### Known Bugs (dari PLAN_PERBAIKAN.md)

| # | Bug | Severity | File | Status |
|---|-----|----------|------|--------|
| 1 | `setMaxDurability()` hardcode 100.0 jika semua fallback gagal | KRITIS | `MMOItemsRepair.java:368` | ❌ Open (v1.1.0) |
| 2 | Lore damaged item overwrite fresh template durability display | KRITIS | `MMOItemsRepair.java:293` | ❌ Open (v1.1.0) |
| 3 | `setMaxDurability()` parameter `type` dan `id` tidak digunakan | KRITIS | `MMOItemsRepair.java:333` | ❌ Open (v1.1.0) |
| 4 | `isDamaged()` Method 3 terlalu agresif (return true jika PDC key exists) | HIGH | `MMOItemsHook.java:462` | ✅ Fixed (v1.0.1) |
| 5 | `isDamaged()` dipanggil 2x redundan | MEDIUM | `GUIListener.java:97` + `ItemUtils.java:371` | ❌ Open |
| 6 | `MMOItemsRepair.canRepair()` tidak cek isDamaged() | MEDIUM | `MMOItemsRepair.java:80` | ✅ Fixed (v1.0.1) |
| 7 | Mace (1.21+) tidak terdeteksi sebagai durability item | HIGH | `ItemUtils.java:175` | ✅ Fixed (v1.0.1) |
| 8 | EXP leak saat MMOItems masuk anvil | HIGH | `AnvilListener.java` (new) | ✅ Fixed (v1.0.1) |
| 9 | `RepairListener implements Listener` tanpa `@EventHandler` | LOW | `RepairListener.java:19` | ❌ Open |
| 10 | WorldGuard terkonfigurasi tapi tidak terimplementasi | LOW | `config.yml` | ❌ Open (v2.0.0) |
| 11 | Update checker terkonfigurasi tapi tidak terimplementasi | LOW | `config.yml` | ❌ Open (v2.0.0) |
| 12 | Pesan error generik "blacklisted" untuk item non-damageable | MEDIUM | `GUIListener.java:104` | ✅ Fixed (v1.0.1) |

---

## 🗺️ Roadmap

### ✅ v1.0.1-SNAPSHOT — Current Release

- [x] Vanilla item repair (all durability items including Mace 1.21+)
- [x] MMOItems integration (API + NBT fallback)
- [x] Interactive GUI (27-slot, 3 buttons)
- [x] Economy integration (Money / XP / Items / Free)
- [x] Bulk repair (Repair All button)
- [x] Blacklist system (4 tiers)
- [x] NBT & enchantment protection (8-point verification)
- [x] Soulbound protection (lore-based)
- [x] AdvancedEnchantments integration (reflection + NBT)
- [x] Customizable messages (messages.yml)
- [x] Color code support (& + hex &#RRGGBB)
- [x] Anvil protection — blok MMOItems dari vanilla anvil (cegah EXP leak)
- [x] Fix isDamaged() Mace & Elytra false negative
- [x] Fix MMOItemsHook.isDamaged() Method 3 agresif
- [x] Fix MMOItemsRepair.canRepair() tanpa isDamaged() check
- [x] Fix pesan error generik "blacklisted" untuk non-damageable items

### 🔜 v1.1.0 — Planned

- [ ] Fix known bugs (setMaxDurability, lore merge, etc.)
- [ ] Oraxen integration
- [ ] ItemsAdder integration
- [ ] Custom repair recipes
- [ ] Repair cooldown system
- [ ] MySQL statistics logging

### 🔮 v1.2.0 — Planned

- [ ] Repair animations
- [ ] Sound & particle customization (partially done)
- [ ] Multi-language support (i18n)
- [ ] BungeeCord / Velocity support

### 🚀 v2.0.0 — Future

- [ ] Web interface for admins
- [ ] Extended API for custom repair types
- [ ] Discord webhook notifications
- [ ] Auction house & shop integration
- [ ] WorldGuard integration (implementasi)
- [ ] Update checker (implementasi)

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

| Jenis Masalah | Tempat Bertanya |
|---------------|-----------------|
| Bug Reports | [GitHub Issues](https://github.com/Syaaddd/OmniRepair/issues) |
| Feature Requests | [GitHub Issues](https://github.com/Syaaddd/OmniRepair/issues) |
| Pertanyaan Umum | Discord (coming soon) |
| Bantuan Konfigurasi | Discord / GitHub Discussions |

### Melaporkan Bug

Sertakan informasi berikut:
1. Versi server (`/version`)
2. Versi plugin (`/plugins` atau cek `plugins/OmniRepair/config.yml`)
3. Daftar plugin aktif
4. Isi `config.yml` (tanpa informasi sensitif)
5. Log console dengan `settings.debug: true`
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
| WorldGuard | GPL-3.0 | Region protection (planned) |

---

<div align="center">

**Enjoy repairing your items safely!** 🔨✨

Made with ❤️ by [Syaaddd](https://github.com/Syaaddd)

[🐛 Report Bug](https://github.com/Syaaddd/OmniRepair/issues) · [💡 Request Feature](https://github.com/Syaaddd/OmniRepair/issues) · [⬇️ Download](https://github.com/Syaaddd/OmniRepair/releases)

</div>
