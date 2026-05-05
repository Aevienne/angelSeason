# AngelSeason

A Minecraft 1.21 plugin that adds a **per-world seasonal cycle** and **Blood Moon system** to your server. Each world can move through Spring, Summer, Autumn, and Winter on its own timer, affecting crop growth, natural mob spawns, and custom seasonal mob availability, while Blood Moon events can trigger during eligible full moon nights and dramatically increase danger.

***

## Features

- Per-world Spring, Summer, Autumn, and Winter progression
- Configurable season duration using custom real-time timers
- Crop growth changes based on the active season
- Full seasonal spawn tables for vanilla mobs via `spawns.yml`
- Custom mob support through configurable seasonal mob ID lists
- Blood Moon events tied to full moon plus a configurable eclipse interval
- Blood Moon can boost hostile mob spawns, health, damage, and speed
- Blood Moon can block sleeping and display alerts through sidebar and boss bar
- Persistent world season and Blood Moon state stored in `data.yml`
- Admin commands for viewing, setting, advancing, and forcing Blood Moon state

***

## Commands

| Command | Description |
|---|---|
| `/season` | View the current season and Blood Moon state for your world |
| `/season status` | View the current season and Blood Moon state |
| `/season set <world> <season>` | Set a world's season manually |
| `/season next <world>` | Advance a world to the next season |
| `/season bloodmoon start <world>` | Force-start a Blood Moon in a world |
| `/season bloodmoon stop <world>` | Force-stop a Blood Moon in a world |
| `/season reload` | Reload the plugin config files |

***

## Permissions

| Permission | Default | Description |
|---|---|---|
| `angelseason.use` | Everyone | Access to season status commands |
| `angelseason.admin` | OP | Access to admin season and Blood Moon commands |

***

## How the Season System Works

Each enabled world tracks its own active season and season timer. When the configured timer expires, that world advances to the next season in order:

- **Spring**
- **Summer**
- **Autumn**
- **Winter**

Each season applies a configurable crop growth multiplier, and mob spawning is filtered through the world’s active seasonal spawn table. Paper exposes crop growth through `BlockGrowEvent`, and natural creature filtering can be handled through spawn events such as `PreCreatureSpawnEvent`.

This means one world can be in Winter with restricted crops and cold-weather mob spawns, while another world is still in Summer with completely different behavior.

***

## How Blood Moon Works

Blood Moon is a separate world event layered on top of seasons:

- It only activates at night
- It can be configured to require a **full moon**
- It only becomes eligible on your configured **eclipse interval**
- It can increase hostile mob spawn pressure
- It can increase hostile mob health, damage, and speed through entity attributes
- It can prevent sleeping during the event

The plugin uses Paper APIs for boss bars, scoreboards, and entity attributes to surface and apply these Blood Moon effects.

***

## Configuration

`config.yml` is generated on first run. Key settings:

```yaml
general:
  tick-interval: 200
  save-interval-seconds: 60
  sidebar-enabled: true
  bossbar-enabled: true
  enabled-worlds: []
  use-all-worlds-when-empty: true

season-lengths-seconds:
  SPRING: 86400
  SUMMER: 86400
  AUTUMN: 86400
  WINTER: 86400

crop-growth-multipliers:
  SPRING: 1.25
  SUMMER: 1.10
  AUTUMN: 0.90
  WINTER: 0.50
```

Blood Moon behavior is configured separately:

```yaml
blood-moon:
  enabled: true
  eclipse-interval-days: 8
  require-full-moon: true
  no-sleep: true
  extra-hostile-spawn-attempts: 2
  health-multiplier: 1.5
  damage-multiplier: 1.35
  speed-multiplier: 1.15
```

Seasonal mob rules are stored in `spawns.yml`. To allow seasonal mobs for a season:

```yaml
spawns:
  WINTER:
    natural:
      allowed:
        - STRAY
        - SKELETON
        - POLAR_BEAR
      blocked:
        - BEE
    custom:
      allowed:
        - frost_walker
        - ice_golem
```

Natural mob filtering relies on spawn event handling, and `plugin.yml` for Paper 1.21 plugins should use `api-version: '1.21'` in the resource descriptor.

***

## Installation

1. Drop the compiled `.jar` into your server's `plugins/` folder
2. Start or restart your server
3. Edit `plugins/AngelSeason/config.yml` to configure season lengths, Blood Moon behavior, and enabled worlds
4. Edit `plugins/AngelSeason/spawns.yml` to define seasonal vanilla and custom mob spawn rules
5. Reload with `/reload confirm` or restart the server

***

## Building

Requires Java 21 and Gradle.

```bash
./gradlew jar
```

Output jar will be in `build/libs/`.

***

## Dependencies

- [Paper API 1.21.11](https://docs.papermc.io/paper/dev/api/) — provided at runtime by the server.
- Paper scoreboard APIs — used for sidebar display and world status panels.
- Paper boss bar APIs — used for Blood Moon alerts.
- Paper attribute APIs — used for hostile mob stat boosts during Blood Moon.
- Custom mob support is config-ready and can be wired to your preferred provider in future integration work
