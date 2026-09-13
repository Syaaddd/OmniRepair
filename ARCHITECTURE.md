# 🏗️ OmniRepair Architecture

## System Overview

OmniRepair uses a **layered architecture** with clean separation of concerns. The plugin follows established design patterns for maintainability and extensibility.

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
│  ├── RepairListener.java                                      │  Bulk repair orchestration + held item repair
│  └── AnvilListener.java                                       │  Anvil protection for MMOItems
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
│  ├── MMOItemsHook.java                                        │  MMOItems API bridge + 4-layer fallback
│  ├── VaultHook.java                                           │  Vault economy bridge
│  └── CustomEnchantHook.java                                   │  AdvancedEnchantments bridge + NBT fallback
├──────────────────────────────────────────────────────────────┤
│  Utility Layer                                                │
│  ├── ItemUtils.java                                           │  Item inspection: durability, blacklist, soulbound
│  ├── LoreUpdater.java                                         │  Color translation (&, hex), lore sync, preview
│  └── NBTProtection.java                                       │  Safe cloning, NBT verification, durability apply
└──────────────────────────────────────────────────────────────┘
```

---

## Design Patterns

### Strategy Pattern

**Purpose:** Encapsulate repair algorithms for different item types.

**Implementation:**
- `RepairHandler` (abstract base) defines the repair contract
- `VanillaRepair` handles standard Minecraft items
- `MMOItemsRepair` handles custom MMOItems equipment

**Benefits:**
- Easy to add new repair strategies (e.g., Slimefun, ItemsAdder)
- Each strategy is independently testable
- Runtime selection based on item type

### Facade Pattern

**Purpose:** Provide unified interface to plugin subsystems.

**Implementation:**
- `OmniRepair` main class exposes high-level methods
- Internal complexity hidden from external callers
- Centralized dependency management

### Observer Pattern

**Purpose:** React to Minecraft events without tight coupling.

**Implementation:**
- `GUIListener` handles inventory interactions
- `RepairListener` manages repair events
- `AnvilListener` prevents unauthorized enchanting

### Dependency Injection

**Purpose:** Reduce coupling, improve testability.

**Implementation:**
- Plugin instance passed via constructors
- All managers receive dependencies at initialization
- No static access to plugin state

### Soft Dependency Pattern

**Purpose:** Graceful degradation when optional plugins are missing.

**Implementation:**
- All integrations check for plugin availability at runtime
- Fallback mechanisms (e.g., NBT reading when API unavailable)
- No hard failures when dependencies are absent

---

## Code Structure

### Source Files

```
src/main/java/com/github/Syaaddd/omniRepair/
├── OmniRepair.java                    # Main plugin class
├── commands/
│   └── RepairCommand.java             # Command executor & tab completer
├── economy/
│   └── EconomyHandler.java            # Economy abstraction (Money/XP/Items/Free)
├── gui/
│   ├── GUIManager.java                # GUI lifecycle manager
│   └── RepairGUI.java                 # GUI inventory implementation
├── integration/
│   ├── CustomEnchantHook.java         # AdvancedEnchantments support
│   ├── MMOItemsHook.java              # MMOItems support (634 lines)
│   └── VaultHook.java                 # Vault economy support
├── listeners/
│   ├── AnvilListener.java             # Anvil protection for MMOItems
│   ├── GUIListener.java               # GUI click/close event handler
│   └── RepairListener.java            # Bulk repair logic
├── repair/
│   ├── RepairHandler.java             # Abstract repair base
│   ├── VanillaRepair.java             # Vanilla repair strategy
│   └── MMOItemsRepair.java            # MMOItems repair strategy
└── utils/
    ├── ItemUtils.java                 # Item inspection utilities
    ├── LoreUpdater.java               # Lore & color utilities
    └── NBTProtection.java             # NBT preservation & verification
```

### Resource Files

```
src/main/resources/
├── plugin.yml                         # Bukkit plugin descriptor
├── config.yml                         # Main configuration (254 lines)
└── messages.yml                       # Message strings (147 lines)
```

---

## Key Components

### RepairHandler (Strategy Base)

**Responsibilities:**
- Define repair contract (`repairItem`, `canRepair`)
- Calculate repair costs
- Handle economy transactions
- Provide rollback safety

**Key Methods:**
```java
public abstract boolean repairItem(Player player, ItemStack item);
public abstract boolean canRepair(ItemStack item);
protected double calculateCost(ItemStack item, double damagePercent);
protected boolean processPayment(Player player, double cost);
```

### MMOItemsHook (Integration)

**Responsibilities:**
- Detect MMOItems custom durability
- Bridge MMOItems API via reflection
- Provide NBT fallback when API unavailable
- Sync lore after repair

**Durability Detection Priority:**
1. MMOItems API (`MMOItems.getDurability()`)
2. NBT PersistentDataContainer (`mmoitems:durability`)
3. Alternative NBT keys (`mmoitems:max_durability`, `mmoitems:current_durability`)
4. Final fallback (`mmoitems:max_hp`)

### NBTProtection (Safety)

**Responsibilities:**
- Clone items before modification
- Verify NBT data preservation
- Safe durability application
- Rollback on failure

**Protection Guarantees:**
- Enchantments preserved
- Display name preserved
- Lore preserved
- Custom model data preserved
- PersistentDataContainer keys preserved

### GUIManager (Lifecycle)

**Responsibilities:**
- Track open GUIs per player
- Prevent duplicate GUIs
- Handle cleanup on disconnect
- Coordinate with RepairGUI

### EconomyHandler (Payment Abstraction)

**Responsibilities:**
- Abstract payment methods (Money/XP/Items/Free)
- Check player balance/sufficiency
- Process payments atomically
- Handle free repair permission

**Payment Flow:**
1. Check `omnirepair.free` permission → skip payment
2. If `use-item-cost` → consume items
3. If `use-xp-cost` → deduct XP levels
4. If `economy.enabled` → withdraw via Vault
5. Otherwise → free repair

---

## Data Flow

### Repair Hand Flow

```
Player clicks "Repair Hand" button
    ↓
GUIListener.handleRepairHandClick()
    ↓
Check permission (omnirepair.hand)
    ↓
Get item in player's hand
    ↓
Validate item (not null, damageable, not blacklisted)
    ↓
Select repair strategy (Vanilla or MMOItems)
    ↓
Clone item for rollback safety
    ↓
Calculate repair cost
    ↓
Process payment via EconomyHandler
    ↓
Apply repair (set damage to 0)
    ↓
Verify NBT preservation
    ↓
Update lore (if enabled)
    ↓
Play sound/particle effects
    ↓
Send success message
```

### Bulk Repair Flow

```
Player clicks "Repair All" button
    ↓
GUIListener.handleRepairAllClick()
    ↓
Check permission (omnirepair.bulk)
    ↓
Scan inventory for damageable items
    ↓
Filter blacklisted items
    ↓
Calculate total cost
    ↓
Check player balance
    ↓
Process payment (all-or-nothing)
    ↓
Repair each item via appropriate strategy
    ↓
Show progress (BossBar)
    ↓
Send bulk success message
```

---

## Configuration System

### config.yml Structure

```yaml
settings:          # Global plugin settings
blacklist:         # Item filtering rules
mmoitems:          # MMOItems integration
economy:           # Payment methods
safety:            # Protection settings
gui:               # GUI appearance
effects:           # Sound/particle effects
worldguard:        # Region-based restrictions
anvil:             # Anvil protection
update-checker:    # Version checking
```

### messages.yml Structure

```yaml
prefix:            # Message prefix
general:           # Common messages (reload, permissions, help)
repair:            # Repair-specific messages
gui:               # GUI-related messages
admin:             # Admin messages
action-bar:        # Action bar messages
boss-bar:          # Boss bar messages
```

---

## Performance Considerations

### Optimizations

1. **Lazy Loading** — Integrations only initialize when needed
2. **Reflection Caching** — MMOItems API methods cached after first lookup
3. **Blacklist Indexing** — Material blacklist uses HashSet for O(1) lookup
4. **GUI Cancellation** — All GUI clicks cancelled early to prevent item manipulation
5. **Bulk Repair Limits** — `max-bulk-repair` prevents inventory scan abuse

### Thread Safety

- All operations run on main server thread (Bukkit requirement)
- No async operations that could cause race conditions
- GUI state tracked per-player in concurrent HashMap

---

## Extension Points

### Adding New Repair Strategies

1. Extend `RepairHandler`
2. Implement `repairItem()` and `canRepair()`
3. Register in `OmniRepair.onEnable()`
4. Add strategy selection logic in `RepairListener`

### Adding New Payment Methods

1. Add method to `EconomyHandler.PaymentMethod` enum
2. Implement payment logic in `processPayment()`
3. Add config options in `config.yml`
4. Update cost calculation if needed

### Adding New Integrations

1. Create hook class in `integration/` package
2. Use soft dependency pattern (check availability)
3. Provide fallback mechanism
4. Register in `OmniRepair.onEnable()`

---

## Testing Strategy

### Manual Testing Checklist

- [ ] GUI opens without errors
- [ ] Vanilla item repair works
- [ ] MMOItems repair works (if installed)
- [ ] Economy deduction works (if Vault installed)
- [ ] Blacklist prevents repair
- [ ] Soulbound items protected
- [ ] NBT data preserved after repair
- [ ] Bulk repair respects limits
- [ ] Anvil protection blocks MMOItems
- [ ] Permissions work correctly

### Debug Mode

Enable with `/repair debug` to see:
- Durability detection details
- Cost calculations
- Payment processing
- NBT verification results
- Integration status

---

## Future Enhancements

### Potential Additions

1. **Repair Animations** — Visual feedback during repair
2. **Statistics API** — Track repairs per player/item
3. **Multi-language Support** — i18n for messages
4. **Region-specific Costs** — Different costs per WorldGuard region
5. **Repair Preview** — Show cost before confirming
6. **Web Dashboard** — Remote management interface

### API Expansion

```java
// Potential future API
public interface OmniRepairAPI {
    boolean repairItem(Player player, ItemStack item);
    double calculateRepairCost(ItemStack item);
    void registerRepairStrategy(RepairHandler strategy);
    void registerPaymentMethod(PaymentMethod method);
}
```

---

**Last Updated:** 2024  
**Version:** 1.0.1-SNAPSHOT
