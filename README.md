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

| Problem | Solusi OmniRepair |
|---------|-------------------|
| Anvil vanilla menghapus enchant | ✅ 100% NBT preservation |
| Item MMOItems tidak terdeteksi rusak | ✅ MMOItems API integration |
| Repair manual memakan waktu | ✅ Bulk repair & GUI interaktif |
| Biaya repair tidak seimbang | ✅ Configurable economy system |

---

## ✨ Features

### 🎮 Universal Durability Detection

- **Vanilla Items** — Tools, armor, weapons, elytra, trident, shield
- **MMOItems** — Full support via official API (bukan NBT parsing)
- **Automatic Lore Sync** — Update durability lore otomatis setelah repair
- **Fallback System** — Tetap berjalan meski MMOItems tidak tersedia

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

- **Live Preview** — Lihat item setelah repair sebelum membayar
- **Cost Display** — Harga transparan ditampilkan di depan
- **Bulk Repair** — Repair seluruh inventory sekaligus
- **RPG Theme** — Dark theme imersif dengan custom styling

### 💰 Economy & Cost System

Tiga metode pembayaran tersedia:

| # | Metode | Deskripsi | Contoh Tampilan |
|---|--------|-----------|-----------------|
| 1 | **Vault Economy** | Integrasi dengan economy plugin manapun | `$10.00` |
| 2 | **XP Levels** | Bayar dengan experience levels | `5 XP Levels` |
| 3 | **Item Cost** | Bayar dengan item tertentu | `10x Iron Ingot` |

### 🛡️ Safety & Protection

| Proteksi | Deskripsi |
|----------|-----------|
| **NBT Preservation** | Enchantment, custom model data, dan lore tetap utuh |
| **Soulbound Check** | Menghormati item soulbound dari plugin lain |
| **Blacklist System** | Blokir material, nama, atau lore tertentu |
| **Cost Cap** | Batas maksimum biaya repair mencegah eksploitasi |
| **Rollback Safety** | Repair gagal akan memulihkan item ke kondisi awal |

---

## 📥 Installation

### Requirements

| Dependency | Versi | Status |
|------------|-------|--------|
| Paper / Spigot / Purpur | 1.21+ | ✅ Required |
| Java | 21+ | ✅ Required |
| MMOItems | 6.9+ | ❌ Optional |
| Vault | 1.7+ | ❌ Optional |
| WorldGuard | 7.0+ | ❌ Optional |

### Quick Install

```
1. Download  →  OmniRepair-1.0.0-SNAPSHOT.jar dari halaman Releases
2. Copy      →  Letakkan JAR ke folder plugins/
3. Start     →  Jalankan server untuk generate config
4. Configure →  Edit plugins/OmniRepair/config.yml
5. Reload    →  /repair reload (opsional)
```

### Starter Config

```yaml
# config.yml — Konfigurasi awal yang direkomendasikan
settings:
  support-mmoitems: true      # Aktifkan jika menggunakan MMOItems
  use-economy: true           # Aktifkan biaya repair
  cost-per-percent: 10.0      # $10 per 1% durability hilang
  max-cost: 5000.0            # Maksimum $5000 per repair

mmoitems:
  enabled: true
  sync-lore: true
  custom-cost-multiplier: 1.5 # MMOItems 50% lebih mahal
```

---

## 🎮 Usage

### Perintah Dasar

```bash
/repair          # Buka repair GUI
/repair hand     # Repair item yang sedang dipegang
/repair all      # Repair semua item di inventory
/repair help     # Tampilkan pesan bantuan
```

### GUI Workflow

```
Step 1 → Letakkan item rusak di slot Input (kiri)
Step 2 → Lihat preview dan biaya di slot tengah/kanan
Step 3 → Klik tombol 🔨 Repair
Step 4 → Ambil item yang sudah diperbaiki!
```

**Contoh Output:**

```
Input   : Diamond Sword (Durability: 500 / 1562)
Preview : Diamond Sword (Durability: 1562 / 1562)
Cost    : $106.50
```

---

## ⚙️ Configuration

### 1. General Settings

```yaml
settings:
  debug: false                # Aktifkan debug mode (console logging)
  support-mmoitems: true      # Dukungan MMOItems
  use-economy: true           # Integrasi economy

  # Konfigurasi biaya
  cost-per-percent: 10.0      # Biaya per 1% durability hilang
  max-cost: 5000.0            # Batas maksimum biaya repair
  min-cost: 5.0               # Biaya minimum repair

  # Toggle fitur
  repair-held-item: true      # Izinkan /repair hand
  bulk-repair: true           # Izinkan /repair all
  max-bulk-repair: 360        # Maks item dalam bulk repair
```

### 2. Blacklist Configuration

```yaml
blacklist:
  # Material yang tidak bisa direpair
  materials:
    - BEDROCK
    - BARRIER
    - COMMAND_BLOCK
    - STRUCTURE_BLOCK
    - ENCHANTED_GOLDEN_APPLE
    - TOTEM_OF_UNDYING
    - ELYTRA           # Opsional: lindungi elytra
    - TRIDENT          # Opsional: lindungi trident

  # Item dengan lore tertentu (case-insensitive)
  lore-contains:
    - "&lEVENT ITEM"
    - "&cNO REPAIR"
    - "Unrepairable"
    - "[Event]"

  # Item dengan display name tertentu
  name-contains:
    - "Event Item"
    - "Admin Item"

  # MMOItems ID yang diblacklist
  mmoitems-ids:
    - "EXAMPLE_SWORD_EVENT"
    - "EXAMPLE_ARMOR_BOSS"
```

### 3. MMOItems Integration

```yaml
mmoitems:
  enabled: true
  custom-cost-multiplier: 1.5   # 1.5 = 50% lebih mahal dari vanilla

  # Otomatis update lore durability setelah repair
  sync-lore: true

  # Format lore (placeholder: {current}, {max}, {percent})
  lore-format:
    enabled: true
    line: "&7Durability: &a{current} &7/ &a{max} &7(&e{percent}%&7)"
    position: "ADD_BOTTOM"      # ADD_BOTTOM | ADD_TOP | REPLACE_EXISTING

  # Pola lore yang akan diganti (jika REPLACE_EXISTING)
  lore-patterns:
    - ".*Durability:.*"
    - ".*HP:.*"
```

### 4. Economy Options

```yaml
economy:
  enabled: true

  # Alternatif: Bayar dengan item
  use-item-cost: false
  cost-item:
    material: "IRON_INGOT"
    amount-per-percent: 0.01

  # Alternatif: Bayar dengan XP
  use-xp-cost: false
  xp-per-percent: 0.5

  # Permission untuk repair gratis
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

  preserve-nbt: true            # Selalu jaga NBT (tidak bisa dinonaktifkan)
  check-balance: true           # Cek saldo sebelum repair
  max-durability-restore: 0     # 0 = tidak terbatas
```

### 6. GUI Customization

```yaml
gui:
  title: "&8&l🔨 RPG Mender"
  size: 54                       # Harus kelipatan 9, maks 54
  background: "BLACK_STAINED_GLASS_PANE"
  background-name: " "

  # Posisi slot
  slots:
    input: 10
    preview: 16
    cost: 28
    repair-button: 31
    repair-all-button: 29
    close-button: 33

  # Material tombol
  buttons:
    repair: "ANVIL"
    repair-all: "HOPPER"
    close: "BARRIER"
    cost-display: "GOLD_BLOCK"

  # Nama tombol
  button-names:
    repair: "&a&lREPAIR ITEM"
    repair-all: "&e&lRepair All Inventory"
    close: "&c&lClose"
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

### Slot Layout (54-Slot GUI)

```
Row 1:  [ 0][ 1][ 2][ 3][ 4][ 5][ 6][ 7][ 8]
Row 2:  [ 9][10][11][12][13][14][15][16][17]
Row 3:  [18][19][20][21][22][23][24][25][26]
Row 4:  [27][28][29][30][31][32][33][34][35]
Row 5:  [36][37][38][39][40][41][42][43][44]
Row 6:  [45][46][47][48][49][50][51][52][53]
```

### Slot Assignments (Default)

| Slot | Posisi | Fungsi |
|------|--------|--------|
| **10** | Row 2, Col 2 | **Input** — Letakkan item rusak di sini |
| **16** | Row 2, Col 8 | **Preview** — Tampilan item setelah repair |
| **28** | Row 4, Col 2 | **Cost Display** — Informasi biaya repair |
| **29** | Row 4, Col 3 | **Repair All** — Bulk repair seluruh inventory |
| **31** | Row 4, Col 5 | **Repair** — Repair item tunggal |
| **33** | Row 4, Col 7 | **Close** — Tutup GUI |

---

## 💰 Economy System

### Formula Perhitungan Biaya

```
Base Cost  = Damage Percent × Cost Per Percent
Final Cost = Base Cost × MMOItems Multiplier (jika berlaku)
           → dibatasi antara Min Cost dan Max Cost
```

### Contoh Kalkulasi

**Vanilla Diamond Sword:**

```
Durability    : 500 / 1562
Kerusakan     : 1062 (68%)
Cost/Percent  : $10.00
Kalkulasi     : 68 × $10 = $680.00
```

**MMOItems Legendary Sword:**

```
Durability    : 200 / 1000
Kerusakan     : 800 (80%)
Cost/Percent  : $10.00
MMO Multiplier: 1.5×
Kalkulasi     : 80 × $10 × 1.5 = $1,200.00
```

### Perbandingan Metode Pembayaran

| Metode | Kelebihan | Kekurangan | Cocok Untuk |
|--------|-----------|------------|-------------|
| **Money** | Simpel, universal | Butuh economy plugin | RPG, Skyblock |
| **XP** | Vanilla-friendly | Grind untuk player | Survival, Hardcore |
| **Items** | Imersif | Butuh farming | Tech, Magic server |

---

## ⚔️ MMOItems Integration

### Alur Kerja

```
1. Detection  →  Cek apakah item adalah MMOItem via MMOItems.getID()
2. Read       →  Ambil current/max durability via MMOItems API
3. Repair     →  Pulihkan durability via MMOItems API
4. Sync       →  Update lore durability otomatis
```

### Fitur yang Didukung

| Fitur | Status |
|-------|--------|
| Custom Durability | ✅ Full support |
| Lore Synchronization | ✅ Full support |
| Type-based Repair | ✅ Full support |
| Stat-based Items | ✅ Full support |
| Custom Models | ✅ Full support |

---

## 🔐 Permissions

| Permission | Deskripsi | Default |
|------------|-----------|---------|
| `omnirepair.use` | Buka repair GUI | `true` (semua player) |
| `omnirepair.hand` | Gunakan `/repair hand` | `true` (semua player) |
| `omnirepair.bulk` | Gunakan `/repair all` | `false` (OP only) |
| `omnirepair.admin` | Perintah admin (`reload`, `debug`) | `op` |
| `omnirepair.free` | Repair gratis (bypass semua biaya) | `false` |

### Contoh Setup Permission

**Survival Server:**

```yaml
permissions:
  omnirepair.use: true
  omnirepair.hand: true
  omnirepair.bulk: false    # Hanya OP yang bisa bulk repair
```

**RPG Server (dengan VIP):**

```yaml
groups:
  vip:
    permissions:
      - omnirepair.use
      - omnirepair.hand
      - omnirepair.bulk
      - omnirepair.free     # VIP dapat repair gratis!
  default:
    permissions:
      - omnirepair.use
      - omnirepair.hand
```

---

## 💬 Commands

### Player Commands

| Command | Deskripsi | Permission |
|---------|-----------|------------|
| `/repair` | Buka repair GUI | `omnirepair.use` |
| `/repair hand` | Repair item yang dipegang | `omnirepair.hand` |
| `/repair all` | Repair semua item inventory | `omnirepair.bulk` |
| `/repair help` | Tampilkan pesan bantuan | `omnirepair.use` |

### Admin Commands

| Command | Deskripsi | Permission |
|---------|-----------|------------|
| `/repair reload` | Reload semua file konfigurasi | `omnirepair.admin` |
| `/repair debug` | Toggle debug mode | `omnirepair.admin` |

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
