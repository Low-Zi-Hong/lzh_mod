"# lzh_mod" 
# 👁️ LZH Utility Mod (Perception & Assist Module)

> **"The 'Eyes' and 'Hands' of the autonomous agent system. A comprehensive utility mod for resource location, inventory management, and survival assurance."**

![Java](https://img.shields.io/badge/Language-Java_21-orange) ![Fabric](https://img.shields.io/badge/Framework-Fabric_1.21-blue) ![License](https://img.shields.io/badge/License-MIT-green)

While the **`wtf`** mod handles *locomotion and pathfinding* (The Legs), **`lzh_mod`** handles *perception, interaction, and survival* (The Eyes & Hands). It replaces the functionality of outdated utility mods (like Mob/Meteor) with custom, lightweight implementations tailored for technical gameplay and automation.

---

## ⚡ Core Features

### 1. Advanced Perception (ESP & Search)
* **Block Search Engine:**
    * **Source:** `lzhong.net.lzh.CustomCommand.SearchBlocks`
    * **Function:** Scans the loaded chunks for specific blocks (e.g., Diamond Ore, Ancient Debris) and highlights them with a high-contrast outline (`HighlightBlocks`) visible through walls.
    * **Integration:** Provides target coordinates for the `wtf` bot to autonomously navigate to and mine.
* **Global Item Tracker:**
    * **Source:** `lzhong.net.lzh.CustomCommand.FindItem`
    * **Logic:** Maintains a memory database of all opened containers (`ContainerInfo`). It records the contents of every chest/barrel the player interacts with.
    * **Usage:** Allows the user to instantly search for an item (e.g., "Enchanted Book") and retrieve its exact location (Coordinates + Slot ID) from the database, eliminating the need to manually rummage through storage systems.

### 2. Survival Assurance (Auto-Disconnect)
* **Source:** `lzhong.net.mixin.PlayerDamageMixin`
* **Logic:** Injects logic into the player's damage handling. It calculates incoming damage *before* it is applied.
* **Fail-safe:** If `Incoming Damage >= Current Health` (and no Totem of Undying is active), the mod **instantly disconnects** the client from the server to prevent death and gear loss.
* *Display:* Shows a "You would have died!" screen upon disconnect.

### 3. Redstone & Building Automation
* **Item Sorter Helper:**
    * **Source:** `lzhong.net.lzh.CustomCommand.AutoFillFilterItem`
    * **Function:** Automatically fills the held item into a hopper in a specific pattern (1-1-1-1-41) used for standard impulse item filters, significantly speeding up the construction of storage systems.
* **Smart Material Switcher:**
    * **Source:** `lzhong.net.MainInitialiser`
    * **Logic:** Detects interaction context. For example, clicking on Glass with Wool (or vice versa) automatically swaps the hand to the correct material, streamlining color-coded builds.

---

## 🛠️ Commands

| Command | Arguments | Description |
| :--- | :--- | :--- |
| `/lzh find` | `<block_name>` | Scans radius for the specified block and renders outlines. |
| `/lzh findItem` | `<item_name>` | Searches the internal database for the item location in known chests. |
| `/lzh fillFilter` | `[None]` | Auto-configures the target hopper for item sorting. |
| `/lzh look` | `<block_name>` | Raycasts to find specific blocks in line of sight. |

---

## 📂 Project Structure

text
src/main/java/lzhong/net
├── lzh/
│   └── CustomCommand/
│       ├── SearchBlocks.java      # X-Ray / Block Scanner
│       ├── FindItem.java          # Item Database & Search
│       ├── AutoFillFilterItem.java# Redstone Helper
│       ├── HighlightBlocks.java   # Render System for ESP
│       └── ContainerInfo.java     # Data Structure for Chest Memory
├── mixin/
│   └── PlayerDamageMixin.java     # Auto-Log / Anti-Death Logic
└── MainInitialiser.java           # Event Handlers (Smart Build)

---

## 🔗 Integration with wtf Bot
This mod is designed to work in tandem with the Minecraft Client Bot (wtf):

Locate: Use /lzh find <ore> to detect target blocks.

Target: Extract coordinates from the lzh highlight.

Navigate: Feed coordinates to /wtf debug wtfAlgo x y z to initiate autonomous pathfinding and mining.

---

##💻 Tech Stack
Category	Technology
Language	Java 21
Framework	Fabric Loader + Fabric API
Game Version	Minecraft 1.21
Key Techniques	Mixins, Raycasting, RenderSystem, NBT Manipulation
