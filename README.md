# 🎴 Cobblemon Cards

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue.svg?style=for-the-badge&logo=minecraft&logoColor=white)](https://minecraft.net)
[![Fabric Loader](https://img.shields.io/badge/Loader-Fabric-lightgrey.style?style=for-the-badge&logo=fabric)](https://fabricmc.net)
[![Cobblemon Compatible](https://img.shields.io/badge/Cobblemon-Compatible-orange.svg?style=for-the-badge&logo=pokemon)](https://cobblemon.com)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg?style=for-the-badge)](LICENSE)

An immersive and feature-rich Minecraft Fabric mod that introduces the **ultimate Trading Card Game** to the **Cobblemon** universe! Collect, trade, grade, and proudly display your favorite Pokémon on gorgeous 3D cards complete with animated holographic shaders.

---

## 🌟 Key Features

### 📦 Thematic & Generational Booster Packs
* **Over 20 distinct booster packs** to open using an interactive custom opening interface.
* **Generations 1 to 9**: Focus your collection on specific regions and generations!
* **Type-Themed Boosters**: Target your search with elemental packs containing only specific types (Fire, Water, Grass, Electric, Ghost, etc.).
* **God Pack Ticket**: A legendary item that guarantees your next booster pack will be an ultra-rare "God Pack"!

### 🎴 Collectible Pokémon Cards
* **Multiple Rarity Tiers**: *Common*, *Uncommon*, *Rare*, *Epic*, *Legendary*, and *Mythic*.
* **Visual Variants**: Normal cards and full-art **Shiny** (Chromatiques) variants with custom visual models.
* **Special Forms**: Complete integration of Regional variants (Alola, Galar, Hisui) and Mega Evolutions.

### ✨ Dynamic Holographic Shaders & Visual Effects
Witness over **20 unique procedural holographic effects** powered by custom shaders that glisten and shift as you look around:
* *Foil Stars, Rainbow Prism, Plasma, Cosmic Constellation, Cyber Dust, Magical Wind...*
* Custom special-form effects like *Mega Vortex, Alolan Shore, Galarian Steam, Paldean Terastal, Distortion Rift, Time Gears, Spatial Crack, and Prism Stars...*

### 📖 Card Binders & Storage Cabinets
* **Tiered Binders**: Craft Leather, Iron, Gold, Diamond, Netherite, and the ultimate **Master Album**.
* **RPG Stats & Passive Bonuses**: Equip your binders in your Trinkets slot! Slotted cards grant passive stat boosts (Mining Speed, Movement Speed, Attack Damage, Luck, Armor, Max Health, and custom wild spawn rate multipliers).
* **Card Cabinet**: A beautiful piece of furniture storing up to **12,000 cards** featuring built-in search, sorting, and filter controls.

### 🔬 Grading Station & Recycling System
* **Grading Station**: Analyze and rate your cards (Grades 1 to 10). High-grade cards grant massive multipliers to active RPG stats!
* **Card Recycler**: Grind duplicate or unwanted cards down into **Cobblecard Dust**.
* Use dust to power scanning equipment or speed up the card grading process.

### 📡 Instant-Dex Tool & Structure Disks
* **Instant-Dex Scanner**: A handheld utility tool to scan wild Pokémon in the wild.
* **Card Structure Disks**: Load them with card dust, lock in a target species, scan them in the wild, and print a physical card once compilation hits 100%!

### 🌌 3D Holographic Projectors
* Showcase your trophy cards in your base using regular and advanced **Holo Projectors**.
* **6 Display Modes**: Continuous Rotation, Face Player, Dynamic (Spin & Face), Fixed, Flat, and Simple Bobbing.
* The advanced projector lets you slot in and sequence a moving gallery of up to **27 cards**!

---

## 🛠️ Required Dependencies

To run **Cobblemon Cards**, download and place the following mods inside your client or server `mods` directory:

| Mod | Required Version | Purpose |
| :--- | :--- | :--- |
| **Fabric API** | `0.116.10+1.21.1` | Core Fabric library |
| **Cobblemon** | Compatible 1.21.1 | Core Pokémon mod |
| **Architectury API** | `13.0.6` | Cross-platform compatibility helper |
| **Cloth Config** | `15.0.140` | Mod configuration and UI systems |
| **Trinkets** | `3.10.0` | Enables accessory slots to equip binders for RPG stats |
| **MidnightLib** | `1.9.2+1.21.1` | Lightweight config library |
| **Cardinal Components** | `6.1.3` | Entity data attachment system |

> [!TIP]
> **Recipe Viewers (EMI, REI, JEI):** The mod fully integrates recipe viewers compileOnly contracts. This makes it easy for players to check custom crafting recipes for Binders, Cabinets, Recyclers, and the Grading Station in-game!

---

## 🚀 Installation Guide

1. Download and install **Fabric Loader** for Minecraft version `1.21.1`.
2. Grab the dependencies listed above and place them into your `.minecraft/mods` folder.
3. Download or compile the **Cobblemon Cards** `.jar` file and drop it into the `mods` folder.
4. Launch the game and start your ultimate collection journey!

---

## 💻 For Developers: Compiling from Source

If you want to modify the source code or build the mod manually:

1. Clone this repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/CobblemonCards.git
   cd CobblemonCards
   ```
2. Decompile and set up the Minecraft environment:
   ```bash
   ./gradlew genSources
   ```
3. Build the mod JAR (found in `build/libs/` after compilation):
   ```bash
   ./gradlew build
   ```

---

## 📜 License

This project is licensed under proprietary terms. All rights reserved. Code modification and asset distribution are subject to written approval from the repository owner.

---

*Made with ❤️ by Pokemon card enthusiasts in Minecraft.*
