# 🔧 OmniRepair - Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.1-SNAPSHOT] - Current Development

### 🎯 Added

#### Anvil Protection System
- **PrepareAnvilEvent handler** — Blocks MMOItems from being enchanted in vanilla anvils
- **InventoryClickEvent handler** — Prevents taking enchanted MMOItems from anvil result
- **Config control** — `anvil.block-mmoitems-enchant: true/false`
- **Type filtering** — Optional whitelist for allowed MMOItems types (`anvil.allowed-mmoitems-types`)
- **Purpose** — Prevents EXP leak when players accidentally combine MMOItems with books

**Files Changed:**
- `listeners/AnvilListener.java` (new)
- `config.yml` (anvil section added)
- `messages.yml` (anvil-blocked message added)

---

### 🔧 Changed

#### GUI Simplification
- **Simplified from 54-slot to 27-slot GUI** — Cleaner, faster interface
- **Reduced to 3 buttons** — Repair Hand, Repair All, Close
- **Removed input slots** — No more confusing item placement
- **Removed preview system** — Instant repair without confirmation
- **Improved UX** — Players can repair items directly from inventory

**Old GUI (54 slots):**
```
┌─────────────────────────────────────────────────────────┐
│  [Input] [Preview] [Cost] [Confirm] [Cancel] ...       │
│  [  ] [  ] [  ] [  ] [  ] [  ] [  ] [  ] [  ] ...    │
│  ... (6 rows total)                                     │
└─────────────────────────────────────────────────────────┘
```

**New GUI (27 slots):**
```
┌─────────────────────────────────────────────────────────┐
│  [    ] [    ] [    ] [    ] [Close] [    ] [    ] ... │
│  [    ] [Hand] [    ] [ All ] [    ] [    ] [    ] ... │
│  [    ] [    ] [    ] [    ] [    ] [    ] [    ] ... │
└─────────────────────────────────────────────────────────┘
```

**Files Changed:**
- `gui/RepairGUI.java` (complete rewrite)
- `gui/GUIManager.java` (updated for new structure)
- `listeners/GUIListener.java` (new handlers: `handleRepairHandClick()`, `handleRepairAllClick()`)
- `config.yml` (new slot configuration)
- `messages.yml` (updated button messages)

#### MMOItems Custom Durability - NBT Fallback
- **Multi-layer durability detection** — 4 fallback layers when API unavailable
- **NBT reading** — Reads durability from PersistentDataContainer
- **NBT repair** — Directly modifies NBT data when API methods fail
- **Improved compatibility** — Works across more MMOItems versions

**Durability Detection Priority:**
1. MMOItems API via reflection (primary)
2. NBT `mmoitems:durability` (fallback)
3. NBT `mmoitems:max_durability` (fallback)
4. NBT `mmoitems:current_durability` (fallback)
5. NBT `mmoitems:max_hp` (last resort)

**Files Changed:**
- `integration/MMOItemsHook.java` (added `readDurabilityFromNBT()`, `repairViaNBT()`)

#### Configuration Improvements
- **Reorganized sections** — Logical grouping with clear separators
- **Improved comments** — Better documentation for each setting
- **Added examples** — Clear usage examples for complex options
- **Fixed ordering** — `update-checker` section moved to proper location

**Files Changed:**
- `config.yml` (complete restructure)

---

### 🐛 Fixed

- **NBT data loss** — Fixed issue where custom enchantments could be lost during repair
- **Soulbound bypass** — Fixed exploit where soulbound items could be repaired
- **Cost calculation** — Fixed incorrect cost for partially damaged items
- **GUI duplication** — Fixed issue where multiple GUIs could be opened simultaneously
- **Permission checks** — Fixed missing permission checks in bulk repair

---

### 📝 Documentation

- **README.md** — Simplified to essential information (~250 lines)
- **ARCHITECTURE.md** — New detailed architecture documentation
- **BUILDING.md** — Updated build instructions
- **CHANGELOG.md** — This file, following Keep a Changelog format
- **CONTRIBUTING.md** — New contribution guidelines

---

## [1.0.0] - Initial Release

### 🎯 Core Features

#### Universal Durability Detection
- Vanilla item durability detection (tools, armor, weapons, elytra, trident, shield)
- MMOItems custom durability detection via API
- Automatic lore sync after repair
- Fallback system for missing dependencies

#### Repair GUI System
- 54-slot GUI with input/preview/cost display
- Repair hand, repair all, bulk repair options
- Real-time cost calculation
- Preview of repair results

#### Economy Integration
- Vault economy support (money)
- XP level costs
- Item-based costs
- Free repair permission

#### Safety & Protection
- NBT preservation during repair
- Soulbound item protection
- Blacklist system (materials, lore, names, MMOItems IDs)
- Cost caps (min/max)
- Rollback safety (clone before repair)

#### Plugin Integrations
- MMOItems 6.9+ (soft dependency)
- Vault 1.7+ (soft dependency)
- WorldGuard 7.0+ (config only)
- AdvancedEnchantments (soft dependency)

#### Commands & Permissions
- `/repair` — Open GUI
- `/repair <player>` — Open GUI for player
- `/repair reload` — Reload config
- `/repair debug` — Toggle debug mode
- Permission nodes for all features

#### Configuration
- Fully configurable GUI (materials, names, lore)
- Economy settings (costs, multipliers)
- Blacklist management
- MMOItems integration options
- Safety protections
- Visual effects (sounds, particles, action bar)

---

## Version History Summary

| Version | Release Date | Key Changes |
|---------|--------------|-------------|
| 1.0.1-SNAPSHOT | In Development | GUI simplification, anvil protection, NBT fallback |
| 1.0.0 | Initial | Full feature set released |

---

## Upcoming Features (Roadmap)

- [ ] Multi-language support (i18n)
- [ ] Repair animations and particle effects
- [ ] Region-specific repair costs
- [ ] Repair preview before confirmation
- [ ] Statistics tracking (repairs, economy)
- [ ] Web API for remote management
- [ ] More plugin integrations (Slimefun, ItemsAdder)

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to this project.

---

**For full commit history, see:** https://github.com/Syaaddd/OmniRepair/commits/main
