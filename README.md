# Weight Challenge

Mod Minecraft **multi-loader** (Fabric + NeoForge) inspiré du défi « Je suis obèse et je dois maigrir » : chaque joueur a un **poids en kg**, le perd ou le gagne selon ses actions, voit son poids en **HUD**, et sa **taille** évolue via l’attribut vanilla `generic.scale`.

## Versions supportées

| Minecraft | Fabric API (exemple) | NeoForge (exemple) | Java |
|-----------|----------------------|--------------------|------|
| **1.21.11** | `0.139.5+1.21.11` | `21.11.3-beta` | 21 |
| **26.1.2** | `0.155.3+26.1.2` | `26.1.2.27-beta` | 25 |
| **26.2** | `0.161.0+26.2` | `26.2.0.93` | 25 |
| **26.3** | `0.165.0+26.3` | `26.3.0.99` | 25 |

Les versions sont définies dans `versions/<version>.properties`. La version active par défaut est **1.21.11** (`gradle.properties` → `mcVersion`).

## Mécaniques

### Poids

- **Départ** : 450 kg (configurable)
- **Objectif affiché** : 90 kg (configurable, indicatif)
- **HUD** : action bar (ou variante « overlay » plus visible) mis à jour en temps réel
- **Multijoueur** : le poids est stocké côté serveur (attachement joueur), synchronisé vers le client pour le HUD ; l’échelle utilise `Attributes.SCALE` côté serveur

### Perte de poids (valeurs par défaut)

| Action | Effet |
|--------|--------|
| Marcher / se déplacer | −0,1 kg / s |
| Sprinter (si autorisé) | −0,1 kg / s |
| Nager | −0,15 kg / s |
| Miner | −0,08 kg / s |
| S’accroupir (chaque activation) | −0,2 kg |
| Sauter | −0,5 kg |
| Frapper une entité | entre −0,05 et −0,6 kg (aléatoire, bornes configurables) |

### Gain de poids

| Action | Effet |
|--------|--------|
| Rester immobile | +0,1 kg / s |
| Manger | selon l’aliment (table JSON, ex. pastèque +0,3 kg, steak ~+6 kg, défaut +0,5 kg) |

### Effets liés au poids

- Au-dessus du **seuil de sprint** (défaut 180 kg) : le joueur **ne peut plus sprinter**
- **Taille** : interpolation entre `scaleAtMinWeight` et `scaleAtMaxWeight` selon le poids (références 90–450 kg par défaut)
- **Chutes** (optionnel) : au-dessus de 200 kg, dégâts de chute multipliés (×1,5 par défaut)

## Commandes (OP / niveau GameMaster)

```
/weight set <joueur> <kg>
/weight add <joueur> <delta>
/weight reset <joueur>
/weight get [joueur]
```

## Configuration

Au premier lancement, le mod crée :

`config/weightchallenge.json`

Un exemple complet est fourni dans le dépôt : [`config/weightchallenge.example.json`](config/weightchallenge.example.json).

Tous les taux, seuils, bornes de taille et gains alimentaires y sont modifiables.

## Installation (joueurs)

1. Installer **Fabric** ou **NeoForge** pour votre version de Minecraft.
2. Installer **Fabric API** (Fabric uniquement).
3. Placer le JAR correspondant dans le dossier `mods` :
   - `weightchallenge-fabric-<version>-1.21.11.jar`
   - `weightchallenge-neoforge-<version>-1.21.11.jar`

Les noms exacts dépendent de la version Minecraft compilée (voir `build/libs/`).

## Compilation

Prérequis : **JDK 21** pour 1.21.11, **JDK 25** pour la branche 26.x.

```bash
# Minecraft 1.21.11 (défaut)
./gradlew build

# Autre version
./gradlew build -PmcVersion=26.1.2
./gradlew build -PmcVersion=26.2
./gradlew build -PmcVersion=26.3
```

JARs produits :

- `fabric/build/libs/weightchallenge-fabric-<modVersion>-<minecraftVersion>.jar`
- `neoforge/build/libs/weightchallenge-neoforge-<modVersion>-<minecraftVersion>.jar`

### Lancer le client de dev

```bash
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
```

## Structure du projet

| Module | Rôle |
|--------|------|
| `common` | Logique partagée, config, commandes, mixins, réseau |
| `fabric` | Entrée Fabric, attachements, événements, client HUD |
| `neoforge` | Entrée NeoForge, attachements, événements, client HUD |

Basé sur le [MultiLoader Template](https://github.com/jaredlll08/MultiLoader-Template) (1.21.11).

## Licence

MIT — voir [LICENSE](LICENSE).
