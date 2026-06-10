# 📝 Changelog - Cobblemon Cards

Toutes les modifications majeures apportées à **Cobblemon Cards** dans cette mise à jour.

---

## 🚀 Version 1.0.0 (Mise à jour Multiloader & Easter Eggs)

### ⚙️ Architecture Multiloader (Fabric & NeoForge)
* **Migration Architectury** : Séparation complète du projet en modules `common`, `fabric` et `neoforge`.
* **Support NeoForge** : Le mod tourne désormais nativement sous NeoForge (Minecraft 1.21.1).
* **Gestion des Accessoires** : Support natif de **Trinkets** pour les joueurs Fabric et d'**Accessories** pour les joueurs NeoForge afin d'équiper les classeurs de cartes.
* **Mutualisation des Assets** : Déplacement de l'ensemble des textures, modèles et localisations linguistiques dans le module commun (`common`).

### 🤫 Cartes Easter Eggs (Cosmétiques Mythiques)
Ajout de **6 nouvelles cartes mythiques** purement cosmétiques (Grade 10 parfait, sans statistiques passives et impossibles à recycler) avec des conditions d'obtention uniques à l'aide de l'**Instant-Dex** :
* **Fantôme de Lavanville (`ghost`)** : Scanner un Pokémon spectre (Fantominus, Spectrum, Ectoplasma, Osselait, Ossatueur) vers Minuit (Ticks 16000-20000) en vous tenant sur du *Soul Sand* ou *Soul Soil*.
* **Keunotor Divin (`god_bidoof`)** : Scanner un Keunotor sauvage en tenant une **Pomme dorée** ou une **Pomme dorée enchantée** en main secondaire.
* **Onix de Cristal (`crystal_onix`)** : Scanner un Onix sauvage en tenant un **Éclat d'améthyste** en main secondaire.
* **Lugia Obscur (`shadow_lugia`)** : Scanner un Lugia sauvage sous un **Orage** en subissant l'effet de statut **Wither**.
* **Nymphali Pride (`pride_sylveon`)** : Scanner un Nymphali sauvage en tenant un colorant aux couleurs du drapeau Trans (**Colorant rose**, **bleu clair** ou **blanc**) en main secondaire.
* **Toi & Mew (`you_and_mew`)** : Scanner un Mew sauvage tout en ayant une **Carte de Joueur** (obtenue en scannant un autre joueur avec un disque vierge) dans votre inventaire.

### 💿 Améliorations du Disque de Structure (`card_structure_disk`)
* **Taille de stack** : Réduction de la pile maximale de 64 à **1** pour refléter la valeur de chaque disque unique.
* **Support des Pochons et Sacs de poudre** :
  * Il est désormais possible de charger directement le disque avec des **Pochons de poudre** (9 poudres) et des **Sacs de poudre** (81 poudres).
  * **Clic droit simple** : Consomme une seule unité de la plus petite ressource disponible.
  * **Shift + Clic droit** : Consomme intelligemment et au maximum vos sacs, pochons puis poudres simples pour recharger le disque (limité à 1000).

### 🐛 Corrections de Bugs
* **Correctif Ghost of Lavender Town** : Correction de la détection du bloc sous le joueur. Le fait de s'enfoncer légèrement dans le *Soul Sand* faussait la position Y ; le mod vérifie maintenant à la fois la position actuelle et celle du dessous pour une détection parfaite.
* **Correctif Surnoms** : Remplacement des conditions liées aux surnoms (Keunotor & Nymphali) par des objets tenus en main secondaire, les Pokémon sauvages ne pouvant pas être renommés et le scan de Pokémon possédés étant désactivé pour des raisons de gameplay.
