# 📋 PLAN PERBAIKAN — OmniRepair

**Issue:** Seama OBSI Tools ngga bisa direpair (items dari MMOItems)  
**Tanggal:** 23 Mei 2026

---

## 🔍 RINGKASAN ROOT CAUSE

### Bug #1 — `maxDurability` hardcoded ke 100.0 **(KRITIS)**

| Item | Detail |
|------|--------|
| **File** | `repair/MMOItemsRepair.java:356` |
| **Masalah** | Di method `setMaxDurability()`, nilai `maxDurability = 100.0` dihardcode tanpa membaca dari MMOItems API. |
| **Dampak** | Setelah repair, item OBSI tools jadi durability 100/100 padahal aslinya mungkin 5000/5000. Item **lebih rusak** setelah di-repair. |
| **Fix** | Baca max durability asli dari `MMOItems.plugin.getItem(type, id)` atau `MMOItemsHook.getMaxDurability()`. |

### Bug #2 — Copy lore dari item rusak overwrite fresh template **(KRITIS)**

| Item | Detail |
|------|--------|
| **File** | `repair/MMOItemsRepair.java:180-183` |
| **Masalah** | `repairedMeta.setLore(originalMeta.getLore())` — lore dari item rusak (termasuk display durability) di-copy ke fresh template. Juga `setAttributeModifiers`. |
| **Dampak** | Item hasil repair tetap tampak rusak karena lore durabilitas tidak diperbarui. |
| **Fix** | Jangan copy lore secara mentah. Hanya copy lore non-durability display, atau biarkan lore default dari fresh template. |

### Bug #3 — `setMaxDurability()` parameter `type` dan `id` tidak digunakan

| Item | Detail |
|------|--------|
| **File** | `repair/MMOItemsRepair.java:316-392` |
| **Masalah** | Method menerima `type` dan `id` tapi tidak dipakai untuk lookup max durability. Hanya hardcode 100.0. |
| **Fix** | Gunakan parameter untuk lookup properti item dan dapatkan max durability asli. |

---

## 🐛 BUG LAIN YANG DITEMUKAN

### Bug #4 — `MMOItemsHook.isDamaged()` Method 3 terlalu agresif

| Item | Detail |
|------|--------|
| **File** | `integration/MMOItemsHook.java:419-451` |
| **Masalah** | Jika ada PDC key mengandung "durability", "hp", atau "health", item **selalu dianggap rusak** — bahkan saat durability penuh. |
| **Fix** | Method 3 harus dibandingkan nilai durability saat ini dengan max, bukan asal return true. |

### Bug #5 — `GUIListener.calculateRepairCost()` duplikasi logic perhitungan biaya

| Item | Detail |
|------|--------|
| **File** | `listeners/GUIListener.java:270-288` |
| **Masalah** | Method ini menghitung biaya sendiri terpisah dari `MMOItemsRepair.getRepairCost()` dan `VanillaRepair.getRepairCost()`. Bisa inkonsisten. |
| **Fix** | Panggil handler's `getRepairCost()` langsung, hapus method duplikat. |

### Bug #6 — `isDamaged()` dipanggil dua kali redundan

| Item | Detail |
|------|--------|
| **File** | `listeners/GUIListener.java:102 & 115` |
| **Masalah** | Baris 102 panggil `ItemUtils.isDamaged()`, baris 115 panggil `ItemUtils.canRepair()` yang di dalamnya juga panggil `isDamaged()` lagi. |
| **Fix** | Hapus pengecekan `isDamaged()` terpisah, biarkan `canRepair()` yang handle. |

### Bug #7 — `MMOItemsRepair.isItemDamaged()` selalu return true

| Item | Detail |
|------|--------|
| **File** | `repair/MMOItemsRepair.java:228-262` |
| **Masalah** | Return `true` untuk MMOItems yang punya durability stat **meskipun durability penuh**. Tidak ada pengecekan real. |
| **Fix** | Hapus method atau implementasi logic yang benar. |

### Bug #8 — `GUIListener.hasDurabilityStat()` — Dead Code

| Item | Detail |
|------|--------|
| **File** | `listeners/GUIListener.java:389-447` |
| **Masalah** | Method tidak dipanggil dari mana pun. Juga selalu return `true` untuk MMOItems valid (line 438) jadi meaningless. |
| **Fix** | Hapus method. |

### Bug #9 — `RepairListener` implements Listener tanpa @EventHandler

| Item | Detail |
|------|--------|
| **File** | `listeners/RepairListener.java:20` |
| **Masalah** | `implements Listener` dan didaftarkan via `registerEvents()` tapi tidak ada satu pun method `@EventHandler`. Registrasi percuma. |
| **Fix** | Hapus `implements Listener`, hapus `registerEvents()`. |

### Bug #10 — WorldGuard dikonfigurasi tapi tidak diimplementasi

| Item | Detail |
|------|--------|
| **File** | `config.yml:222-232`, `plugin.yml:12` |
| **Masalah** | Ada section worldguard di config dan softdepend di plugin.yml, tapi tidak ada pengecekan WorldGuard di code. Fitur tidak berfungsi. |
| **Fix** | Implementasi WorldGuard integration atau hapus dari config/plugin.yml. |

### Bug #11 — Update checker dikonfigurasi tapi tidak diimplementasi

| Item | Detail |
|------|--------|
| **File** | `config.yml:238-242` |
| **Masalah** | Ada section update-checker di config tapi tidak ada code untuk mengecek update. |
| **Fix** | Implementasi atau hapus dari config. |

### Bug #12 — Default blacklist terlalu agresif

| Item | Detail |
|------|--------|
| **File** | `config.yml:43-44` |
| **Masalah** | ELYTRA dan TRIDENT di-blacklist secara default. Padahal item legitimate yang punya durability. |
| **Fix** | Hapus ELYTRA dan TRIDENT dari default blacklist. |

---

## 🧹 PERAPIHAN CODE (REFACTORING)

| # | Refactor | File | Keterangan |
|---|----------|------|------------|
| 1 | Pisah `setMaxDurability()` jadi method proper | `repair/MMOItemsRepair.java` | Jangan duplikasi logic, gunakan `MMOItemsHook` yang sudah ada |
| 2 | Cost calculation terpusat | `listeners/GUIListener.java` | Semua perhitungan biaya harus lewat `RepairHandler.getRepairCost()` |
| 3 | Hapus `isItemDamaged()` (duplicate) | `repair/MMOItemsRepair.java` | Sudah ada di `MMOItemsHook.isDamaged()` |
| 4 | Hapus `hasDurabilityStat()` (dead code) | `listeners/GUIListener.java` | Tidak pernah dipanggil |
| 5 | Hapus `isBlacklisted()` duplikat di GUIListener | `listeners/GUIListener.java` | Duplicate dari `ItemUtils.isBlacklisted()` |
| 6 | Perbaiki redundant null/air checks | `listeners/GUIListener.java:73` & `:88` | Double check dengan beda logika |
| 7 | `NBTProtection.verifyNBT()` coverage tidak lengkap | `utils/NBTProtection.java` | Hanya cek enchant, display name, custom model data. Tidak cek lore, PDC keys |
| 8 | Gunakan `NamespacedKey` yang benar | `integration/MMOItemsHook.java` | Namespace `"mmoitems"` bukan milik plugin sendiri, bisa bermasalah |

---

## ✨ PENINGKATAN FITUR (SUGGESTED ENHANCEMENTS)

1. **Tambah WorldGuard region check** — sudah dikonfigurasi tapi belum diimplementasi
2. **Tambah cooldown system** — cegah spam repair
3. **Tambah preview cost di GUI** — tunjukkan biaya sebelum klik
4. **Tambah dukungan multiple payment methods sekaligus** (money + item + XP)
5. **Tambah `/repair hand` dan `/repair all` sebagai command langsung** — permission sudah ada di plugin.yml tapi implementasi tidak ada
6. **Logging lebih baik** — log aktivitas repair siapa, apa, berapa biaya ke file terpisah

---

## ⚙️ URUTAN EKSEKUSI

```
 1  [FIX KRITIS] MMOItemsRepair.java — setMaxDurability() baca max durability asli
 2  [FIX KRITIS] MMOItemsRepair.java — jangan copy lore durability dari item rusak
 3  [FIX]       MMOItemsHook.java — perbaiki isDamaged() Method 3
 4  [FIX]       GUIListener.java — perbaiki cost calculation & redundant checks
 5  [FIX]       RepairListener.java — hapus implements Listener
 6  [FIX]       GUIListener.java — hapus dead code (hasDurabilityStat, isBlacklisted)
 7  [FIX]       config.yml — perbaiki default blacklist
 8  [REFACTOR]  Rapihkan semua code duplicate dan inconsistent patterns
 9  [ENHANCE]   Implementasi WorldGuard jika diperlukan
10  [TEST]      Build, test di server dengan item OBSI tools, verifikasi repair bekerja
```

---

## 📁 FILE YANG AKAN DIUBAH

| File | Prioritas |
|------|-----------|
| `src/main/java/.../repair/MMOItemsRepair.java` | 🔴 KRITIS |
| `src/main/java/.../integration/MMOItemsHook.java` | 🔴 KRITIS |
| `src/main/java/.../listeners/GUIListener.java` | 🟡 SEDANG |
| `src/main/java/.../listeners/RepairListener.java` | 🟢 RENDAH |
| `src/main/resources/config.yml` | 🟢 RENDAH |
| `src/main/java/.../OmniRepair.java` | 🟢 RENDAH |
