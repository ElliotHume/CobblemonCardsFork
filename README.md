# 🎴 Cobblemon Cards

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1-blue.svg?style=for-the-badge&logo=minecraft&logoColor=white)](https://minecraft.net)
[![Fabric Loader](https://img.shields.io/badge/Loader-Fabric-lightgrey.style?style=for-the-badge&logo=fabric)](https://fabricmc.net)
[![Cobblemon Compatible](https://img.shields.io/badge/Cobblemon-Compatible-orange.svg?style=for-the-badge&logo=pokemon)](https://cobblemon.com)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg?style=for-the-badge)](LICENSE)

Un mod Minecraft Fabric complet et immersif qui introduit le **jeu de cartes à collectionner ultime** au sein de l'univers **Cobblemon** ! Collectionnez, échangez, évaluez et affichez fièrement vos Pokémon favoris sous forme de cartes magnifiques dotées de shaders holographiques animés en 3D.

---

## 🌟 Fonctionnalités Principales

### 📦 Boosters Thématiques & Générationnels
* **Plus de 20 boosters différents** à ouvrir avec une interface personnalisée interactive.
* **Générations 1 à 9** : Ciblez les Pokémon d'une génération spécifique !
* **Boosters Élémentaires** : Ouvrez des paquets contenant uniquement des types précis (Feu, Eau, Plante, Électrik, Spectre, etc.).
* **Ticket de God Pack** : Un objet légendaire garantissant que votre prochain booster contiendra uniquement des cartes ultra-rares !

### 🎴 Les Cartes Pokémon de Collection
* **Niveaux de Rareté** : *Common*, *Uncommon*, *Rare*, *Epic*, *Legendary*, *Mythic*.
* **Variantes Visuelles** : Pokémon normaux et Pokémon Chromatiques (Shinies) avec des textures adaptées !
* **Formes Spéciales** : Intégration complète des formes régionales (Alola, Galar, Hisui) et des Méga-Évolutions.

### ✨ Shaders Holographiques & Effets Visuels Révolutionnaires
Découvrez plus de **20 effets holographiques procéduraux** grâce à des shaders personnalisés qui font briller vos cartes sous la lumière du jour :
* *Foil Stars, Rainbow Prism, Plasma, Cosmic Constellation, Cyber Dust, Magical Wind...*
* Des effets uniques pour les formes spéciales comme *Mega Vortex, Alolan Shore, Galarian Steam, Paldean Terastal, Distortion Rift, Time Gears, Spatial Crack, Prism Stars...*

### 📖 Classeurs de Collection (Binders) & Meubles (Cabinets)
* **Progression par Paliers** : Fabriquez des classeurs en cuir, fer, or, diamant ou netherite, ainsi que l'ultime **Master Album**.
* **Statistiques RPG & Bonus Actifs** : Insérer des cartes dans vos classeurs vous octroie des bonus permanents dans le jeu (Vitesse de minage, Vitesse de déplacement, Dégâts d'attaque, Chance, Armure, PV Max, Taux d'apparition de Pokémon spécifiques, etc.).
* **Cabinet à Cartes** : Un meuble en bois élégant capable de stocker jusqu'à **12 000 cartes** avec une interface de tri et de recherche intégrée.

### 🔬 Station d'Évaluation (Grading Station) & Recyclage
* **Station de Graduation** : Analysez vos cartes pour leur attribuer une note (Grade de 1 à 10). Les cartes de Grade supérieur augmentent drastiquement vos bonus de statistiques RPG !
* **Recycleur de Cartes** : Recyclez vos doublons inutiles pour obtenir de la **Poussière de Carte** (*Cobblecard Dust*).
* Utilisez la poussière pour alimenter d'autres outils technologiques ou accélérer vos évaluations.

### 📡 Outil Instant-Dex & Disque de Structure
* **Scanner Instant-Dex** : Un outil technologique permettant de scanner les Pokémon sauvages dans la nature.
* **Disque de Structure** : Chargez-le avec de la poussière de carte pour calibrer une espèce, enregistrez les données de vos rencontres sauvages, et générez votre propre carte physique une fois le scan complété !

### 🌌 Projecteurs Holographiques 3D
* Exposez vos plus belles cartes dans votre base grâce aux **Projecteurs Holographiques** normaux et avancés.
* **Modes d'affichage** : Rotation continue, face au joueur, mode dynamique, fixe, à plat, balancement simple.
* Le projecteur avancé permet de programmer une séquence animée de défilement pour **27 cartes**.

---

## 🛠️ Dépendances Requises

Pour faire fonctionner **Cobblemon Cards**, vous devez installer les mods suivants dans votre dossier `mods` :

| Mod | Version Recommandée | Rôle |
| :--- | :--- | :--- |
| **Fabric API** | `0.116.10+1.21.1` | Bibliothèque de base Fabric |
| **Cobblemon** | Compatible 1.21.1 | Mod principal Pokémon |
| **Architectury API** | `13.0.6` | Bibliothèque de compatibilité multiplateforme |
| **Cloth Config** | `15.0.140` | Gestionnaire de configurations |
| **Trinkets** | `3.10.0` | Permet d'équiper les classeurs pour obtenir les bonus RPG |
| **MidnightLib** | `1.9.2+1.21.1` | Configuration et dépendance système |
| **Cardinal Components** | `6.1.3` | Gestion des données système attachées aux entités |

> [!TIP]
> **Compatibilités optionnelles (EMI, REI, JEI) :** Le mod intègre le support complet des visualisateurs de recettes pour consulter facilement les recettes de craft de la station de graduation, du recycleur et des classeurs.

---

## 🚀 Comment l'installer sur votre serveur ou client

1. Téléchargez et installez le **Fabric Loader** pour Minecraft `1.21.1`.
2. Téléchargez les dépendances mentionnées ci-dessus et déposez-les dans votre dossier `.minecraft/mods`.
3. Compilez ou téléchargez le fichier JAR de **Cobblemon Cards** et placez-le également dans le dossier `mods`.
4. Lancez le jeu et commencez à chasser vos premières cartes !

---

## 💻 Pour les Développeurs : Compiler le projet

Si vous souhaitez modifier le code ou compiler vous-même le mod :

1. Clonez ce dépôt GitHub :
   ```bash
   git clone https://github.com/VOTRE_PSEUDO/CobblemonCards.git
   cd CobblemonCards
   ```
2. Configurez l'environnement de développement Minecraft :
   ```bash
   ./gradlew genSources
   ```
3. Compilez le mod (le fichier JAR sera généré dans `build/libs/`) :
   ```bash
   ./gradlew build
   ```

---

## 📜 Licence

Ce projet est sous licence propriétaire. Tous droits réservés. L'utilisation du code source et des ressources graphiques est soumise à l'autorisation de l'auteur.

---

*Fait avec ❤️ par la communauté des dresseurs de Pokémon sur Minecraft.*
