# Changelog

All notable changes to OmniRepair are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Planned
- Multi-language support (i18n)
- Repair animations and particle effects
- Region-specific repair costs
- Repair preview before confirmation
- Statistics tracking (repairs, economy)
- Web API for remote management

---

## [1.0.1] - 2026-09-13

### Added
- **Anvil Protection** — Block MMOItems from being enchanted via vanilla anvil to prevent EXP leak
- **Config toggle** for anvil protection with optional type whitelist (`allowed-mmoitems-types`)
- **NBT fallback system** for MMOItems durability detection (4-layer fallback via PersistentDataContainer)
- Action bar and boss bar message support
- New repair messages: `not-damageable`, `anvil-blocked`

### Changed
- **GUI simplified** from 54-slot to 27-slot (3 buttons only: Repair Hand, Repair All, Close)
- Reorganized `config.yml` section ordering
- Improved documentation and code comments

### Fixed
- NBT data loss during repair (custom enchantments could be lost)
- Soulbound bypass exploit
- Incorrect cost calculation for partially damaged items
- GUI duplication issue (multiple GUIs opened simultaneously)
- Missing permission checks in bulk repair

---

## [1.0.0] - 2025-05-28

### Added
- **Universal Durability Detection**
  - Vanilla items: tools, armor, weapons, elytra, trident, shield
  - MMOItems custom durability via API (reflection-based)
  - Automatic lore sync after repair
- **Simplified Repair GUI** (27-slot, 3 buttons)
  - Repair Hand — repair item in hand
  - Repair All — repair all damaged items in inventory
  - Close — close GUI
- **Economy System**
  - Vault economy (money)
  - XP level costs
  - Item-based costs
  - Free repair permission (`omnirepair.free`)
- **Safety & Protection**
  - NBT preservation (clone + verify before repair)
  - Soulbound item protection
  - Blacklist system (materials, lore, names, MMOItems IDs)
  - Cost caps (`max-cost`, `min-cost`)
  - Rollback safety (clone item before repair)
- **Plugin Integrations**
  - MMOItems 6.9+ (soft dependency, reflection API + NBT fallback)
  - Vault 1.7+ (soft dependency)
  - WorldGuard 7.0+ (config only)
  - AdvancedEnchantments (soft dependency, preserves custom enchants)
- **Commands**
  - `/repair` — open repair GUI
  - `/repair <player>` — open GUI for another player
  - `/repair reload` — reload configuration (admin)
  - `/repair debug` — toggle debug mode (admin)
- **Permissions**
  - `omnirepair.use` — use repair GUI (default: true)
  - `omnirepair.hand` — use /repair hand (default: true)
  - `omnirepair.bulk` — use /repair all (default: false)
  - `omnirepair.admin` — admin commands (default: op)
  - `omnirepair.free` — bypass all costs (default: false)

---

## Version History

| Version | Date | Highlights |
|---------|------|------------|
| 1.0.1 | 2026-09-13 | GUI simplification, anvil protection, NBT fallback |
| 1.0.0 | 2025-05-28 | Initial release |
