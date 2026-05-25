# Shorin — Projektstruktur

Pathfinder 2e Tabletop-RPG in JavaFX.

---

## Pakete

### `com.fuchsbau.shorin`

- **`Main`** — JavaFX-Einstiegspunkt, hält die primäre `Stage`
- **`Logger/`** — `FileLogger` als globaler Logger

---

### `Engine/`

Technische Basis — kein Spielinhalt hier.

| Paket                             | Inhalt                                                                         |
|-----------------------------------|--------------------------------------------------------------------------------|
| `Engine/Map/`                     | `MapModel` — zentrale Instanz für GameMap + LightingSystem + MapRenderer       |
| `Engine/Map/Core/`                | `MapRenderer`, `MapSaverLoader`, `.shorin`-Binärformat                         |
| `Engine/Map/Core/Tiles/`          | `GameMap` (readonly), `MutableGameMap`, `Tile`, `LocationZone`, `ZoneTrigger`  |
| `Engine/Map/Core/Lighting/`       | `LightingSystem`, `LightMask`, `LightSource`, `IndoorZone`, `Lightlevel`       |
| `Engine/Map/Core/Walls/`          | `WallSegment`, `WallType`                                                      |
| `Engine/Map/Core/Sound/`          | `SoundPoint`                                                                   |
| `Engine/Map/`                     | `Token`, `LightPreset`                                                         |
| `Engine/Editor/`                  | `GameEditor` — Tab-basierter Karten/NPC/Regel-Editor                           |
| `Engine/Editor/Module/`           | `EditorModule` (interface), alle Editor-Tabs implementieren es                 |
| `Engine/Editor/Module/BattleMap/` | `BattleMapModule` — Battlemap-Editor, nutzt `MapModel`                         |
| `Engine/Editor/Module/Actions/`   | `ActionModule` — Aktionen/Spells editieren                                     |
| `Engine/Editor/Module/Classes/`   | `ClassModule`, `FeatModule`, `SpellModule`                                     |
| `Engine/Editor/Module/Races/`     | `AncestryModule`, `HeritageModule`, `LanguageModule`                           |
| `Engine/Encounter/`               | `EncounterPane`, `EncounterState`, `EncounterTransition`                       |
| `Engine/Encounter/Widget/`        | `EncounterWidget` (interface), `InitiativeTrackerWidget`, `DebugOverlayWidget` |
| `Engine/Options/`                 | `GameOptions`, `StyleOptions`                                                  |
| `Engine/Images/`                  | `ImagePreLoader`, `ImagePaths`                                                 |
| `Engine/Styler/`                  | `CSSLoader`                                                                    |

---

### `Engine/RPG/` — Controller-Schicht

| Klasse                         | Rolle                                                                  |
|--------------------------------|------------------------------------------------------------------------|
| `PlayerScreen`                 | **Controller** — verdrahtet Views, routet Input, steuert Screen-Swap   |
| `Saveble`                      | Interface — `getScene(int)` + `reset()` für alle navigierbaren Screens |
| `GameClock`                    | Ingame-Zeitrechnung (Minuten, Stunden, Tag/Nacht) — kein Render-Timer  |
| `AktionBar/ActionMenu`         | Mode-Switch                                                            |
| `AktionBar/TravelingActionBox` | Reise-Aktionen                                                         |
| `AktionBar/CombatActionBox`    | Kampf-Aktionen                                                         |
| `AktionBar/DialogActionBox`    | Dialog-Optionen                                                        |

---

### `Engine/RPG/ViewModules/` — View-Schicht

| Klasse                  | Inhalt                                                  |
|-------------------------|---------------------------------------------------------|
| `Interfaces/Renderable` | `build()` einmalig, `refresh()` nur Daten neu binden    |
| `Interfaces/Hideable`   | `show()`, `hide()`, `isVisible()`, `toggle()`           |
| `LeftPanelView`         | Char-Switcher, Stats, Kreuz-Buttons, Paperdoll          |
| `CenterPanelView`       | Story-Text + ActionMenu / BattleMap-Layer (Screen-Swap) |
| `RightPanelView`        | Minimap (aus `MapModel`), Navigation, Zeit, Reise       |

---

### `Engine/RPG/ui/` — UI-Hilfsmittel

| Klasse          | Inhalt                                                           |
|-----------------|------------------------------------------------------------------|
| `ButtonStyle`   | Enum — alle Button-Typen mit CSS-Klassen                         |
| `ButtonFactory` | Einzige Stelle wo Buttons gebaut werden                          |
| `Actionable`    | Interface für ActionBar-Einträge (Spell, Angriff, Dialog-Option) |

---

### `Engine/System/` — Regelwerk

| Paket                | Inhalt                                                                     |
|----------------------|----------------------------------------------------------------------------|
| `StatBlock`          | Basis aller kämpfenden Einheiten                                           |
| `PlayerCharacter`    | Klasse, Ancestry, Background, Boosts                                       |
| `NonPlayerCharacter` | NPC + Angriffs-Definitionen                                                |
| `ActorData`          | Laufzeit-Zustand (currentHp etc.)                                          |
| `Character/`         | `ClassBuild`, `Skill`, `SenseEntry`, `WeaponCategory`, `AbilityScoreEntry` |
| `Combat/`            | `DamageType`, `DamageModifier`, `ArmorCategory`, `SavingThrows`            |
| `Misc/`              | `Proficiency, `RecallKnowledge`                                            |

---

### `RPG/` — Spielinhalt

| Paket                     | Inhalt                                                 |
|---------------------------|--------------------------------------------------------|
| `Game`                    | Singleton — hält alle Place-Instanzen und Spielzustand |
| `MainScreen`              | Hauptmenü                                              |
| `Intro/`                  | `CharacterCreator`, `WorldStartLocationSelector`       |
| `Places/Place`            | Basis aller Orte — `getSubPlaces()`, `getName()`       |
| `Places/Humanic/Plyport/` | `PlyPort` — instantiiert `PlayerScreen`                |
| `Places/Whitebridge/`     | Inn, Shop, Library, Barracks, Entrance                 |

---

## Architektur-Regeln

- **`MapModel` wird injiziert** — kein Modul baut eigene `GameMap`/`LightingSystem`/`MapRenderer`
- **Views wissen nicht was passiert** — Callbacks kommen vom Controller
- **Ein `AnimationTimer`** — `GameLoop`, `GameClock` bekommt `tick(delta)`, kein eigener Timer
- **Dirty-Flags** — `mapDirty`, `lightDirty`, `tokensDirty` steuern was neu gerendert wird
- **Buttons nur über `ButtonFactory`** — kein inline `getStyleClass().add()`
