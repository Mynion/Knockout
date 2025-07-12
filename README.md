![Banner](https://i.imgur.com/TSBMTP6.png)

**Knockout** is a Spigot plugin that introduces a unique *knockout* mechanic as an alternative to direct player death. Instead of dying instantly, players can be incapacitated and revived or carried to safety. This opens the door for more strategic, team-oriented gameplay and immersive PvP or PvE scenarios.

## 🔥 Features

- Players are knocked out instead of dying.
- Revive knocked-out players by sneaking near them.
- Carry downed players to safe locations.
- Fully configurable system (knockout time, effects, behavior).
- Permissions and admin commands included.
- PlaceholderAPI support.
- Custom messages and titles.
- Compatible with Minecraft versions **1.19.x to 1.21.x**.

## 🛠️ Installation

1. Download the latest `.jar` release from [spigotmc.org](https://www.spigotmc.org/resources/knockout.119934/).
2. Place it into your server's `plugins/` directory.
3. Restart or reload the server.
4. Modify `config.yml` if needed.

## 💬 Support & Community

Join our [Discord server](https://discord.com/invite/ynwDP8H2DY) for support, feature requests, and updates!

## 🤝 Contributing

Contributions are welcome!  
If you'd like to help improve this plugin, please read the [Contributing Guidelines](CONTRIBUTING.md) before submitting a pull request.

---

<details>
  <summary>⚙️ <strong>View <code>config.yml</code></strong></summary>

```yaml
# This plugin is compatible with PlaceholderAPI
# However, there are some placeholders available in the config itself
# There is also a few custom placeholders for PlaceholderAPI described on the plugin's page

# You can use RGB colors in messages, example: knockout-title: '&#2FD0AAKnockout'

# Time of knockout in seconds
knockout-time: 60

# Amount of xp levels required to revive a player
revive-levels: 5

# Time in seconds to revive a player
revive-time: 10

# Item required in main hand of a rescuer to revive a player, eq. 'GOLDEN_APPLE'. Set to '' to disable
revive-item: ''

# Send message to player after attempt to revive a player with an item missing in main hand
revive-item-missing-message: '&cYou need a golden apple in your main hand!'

# When true: add slowness effect for the player carrying a knocked out player
slowness-for-carrier: true

# Slowness amplifier for the player carrying a knocked out player; Slowness level is equal to amplifier + 1
slowness-amplifier: 0

# When true: when carrying player is hit, drop the knocked out player
drop-on-hit: true

# If true knocked out player will die when the knockout time ends otherwise will be revived
death-on-end: true

# When true: player will have blindness effect when knocked out
knockout-blindness: true

# Blindness amplifier for the knocked out player; Blindness level is equal to amplifier + 1
blindness-amplifier: 0

# When true: player will be able to move when knocked out
move-when-knocked-out: false

# When true: player will be able to swim when knocked out
swim-when-knocked-out: false

# When true: player will be able to jump when knocked out
jump-when-knocked-out: false

# When true: it will be possible to attack knocked out players
knockout-vulnerable: true

# When true: knocked out player will take damage on hit, else his knockout time will decrease
damage-on-hit: true

# Health of revived player after revival
# Set to -1 to set the maximum health of the player
# Set to 0 to not change the health
# Or set to a specific value to set the health of the revived player
revived-health: 0.0

# Allow looting a knocked out player by clicking RightClick on him
looting-allowed: false

# If damage-on-hit is false, it is amount of knockout time to be decreased on hit
time-decrease-on-hit: 5

# When true: players can be revived by instant health 2 potion
revive-by-instant-health: false

# When true: players can be carried by Shift+RightClick and dropped by Shift+LeftClick
click-to-carry-drop: true

# When true: player will respawn in the specific location given below
# Works only on 1.20.6+ versions
# WARNING: If true, the player will respawn in this specific location even if it is not safe! Make sure to set it to a safe location!
respawn-in-custom-location: false

# Specific location where the player will respawn
# If world-name is empty or incorrect, the player will respawn in the same world where he was knocked out
custom-location:
  world-name: world
  x: 0
  y: 100
  z: 0

# When true: players will get knocked out only in the worlds listed in world-whitelist
enable-world-whitelist: false

# Knockouts will work only in the worlds listed below if enable-world-whitelist is true.
world-whitelist:
  -

# Knockouts will not work in the worlds listed below. Players will die instead.
world-blacklist:
  -

# Note that "/ko knockout" command will work in all worlds regardless of the whitelist/blacklist

# Message for the player when knocked out
knockout-message: '&cYou have been knocked out! Ask someone to revive you or use /die to die at once.'

# Title message for the player when knocked out
knockout-title: '&cKnockout'

# Subtitle message for the player when knocked out
# Use variable %timer% to show the remaining knockout time
knockout-subtitle: '%timer%'

# Hologram title above the player when knocked out
knockout-hologram: '&cKnockout'

# Message for the player who knocked out another player
# Use variable %player% to show the knocked out player name
knockout-attacker-message: '&aYou knocked out &f%player%'

# Title message for the player who knocked out another player
# Use variable %player% to show the knocked out player name
knockout-attacker-title: ''

# Subtitle message for the player who knocked out another player
# Use variable %player% to show the knocked out player name
knockout-attacker-subtitle: ''

# Message for not allowed actions when knocked out
not-allowed-message: "&cYou can't do that when knocked out!"

# Message for the player with not enough levels to revive
# Use variable %levels% to show the required levels
no-levels-message: "&cYou don't have enough levels (%levels%) to revive that player"

# Title message for the rescuer when reviving
# Use variable %percent% to show the progress of reviving
# Use variable %loading-icon% to show the loading icon
# Use variable %player% to show the rescued player name
rescuer-reviving-title: '&aReviving...'

# Subtitle message for the rescuer when reviving
# Use variable %percent% to show the progress of reviving
# Use variable %loading-icon% to show the loading icon
# Use variable %player% to show the rescued player name
rescuer-reviving-subtitle: '%percent%%'

# Title message for the rescuer when revived
# Use variable %player% to show the rescued player name
rescuer-revived-title: '&aYou revived &f%player%'

# Subtitle message for the rescuer when revived
# Use variable %player% to show the rescued player name
rescuer-revived-subtitle: ''

# Successful revive message for the rescuer
# Use variable %player% to show the rescued player name
rescuer-revived-message: ''

# Title message for the rescued when reviving
# Use variable %percent% to show the progress of reviving
# Use variable %loading-icon% to show the loading icon
# Use variable %player% to show the rescuer player name
rescued-reviving-title: '&aYou are being revived...'

# Subtitle message for the rescued when reviving
# Use variable %percent% to show the progress of reviving
# Use variable %loading-icon% to show the loading icon
# Use variable %player% to show the rescuer player name
rescued-reviving-subtitle: '%percent%%'

# Title message for the rescued when revived by a player
# Use variable %player% to show the rescuer player name
rescued-revived-by-title: '&aYou live again!'

# Subtitle message for the rescued when revived by a player
# Use variable %player% to show the rescuer player name
rescued-revived-by-subtitle: '&aYou have been revived by &f%player%'

# Successful revive message for the rescued by a player
# Use variable %player% to show the rescuer player name
rescued-revived-by-message: ''

# Title message for the rescued when revived by other reason
rescued-revived-title: '&aYou live again!'

# Subtitle message for the rescued when revived by other reason
rescued-revived-subtitle: '&aYou have been revived'

# Successful revive message for the rescued by other reason
rescued-revived-message: ''

# Message when the player uses /die command and is not knocked out
invalid-die-message: "&cYou can use that command only when knocked out!"

# Message when the player uses /drop command and is not carrying any knocked out player
invalid-drop-message: "&cYou are not carrying any knocked out player!"

# Message when the player uses /carry command and there is no knocked out player nearby
invalid-carry-message: "&cThere is no knocked out player nearby!"

# Message when the player uses /carry command and is already carrying a knocked out player
already-carrying-message: "&cYou are already carrying a knocked out player!"

# Message when the player doesn't have permission to do something
no-permission-message: "&4You don't have permission to do that!"

# Aliases for the commands
# Restart the server after changing the aliases
aliases:
  carry:
    - podnies
    -
  drop:
    - zrzuc
    -
  die:
    - zgin
    -
  knockout:
    - ko

# USE WITH CAUTION
# Commands to run from console.
# Variable %player% is the knocked out player
# Use DELAY <ticks> to delay the command execution
# 20 ticks = 1 second
# Example:
#   - playsound minecraft:entity.zombie.death player %player% ~ ~ ~ 10 1 1
#   - say &cPlayer fainted from breaking his legs
#   - DELAY 40
#   - say &eWill anyone help &f%player%&e?

# Enable console commands.
enable-console-commands: false

# Run console commands when a knockout happens.
console-knockout-commands:
  -

# Run console commands during knockout on a loop.
# Use DELAY <ticks> to delay commands execution or the server will crash
console-knockout-loop-commands:
  -

# Run console commands after knockout.
console-after-knockout-commands:
  -

# Run console commands after revive.
console-after-revive-commands:
  -
```
</details>

---

<details>
  <summary>📜 <strong>Commands</strong></summary>

| Command                                 | Description                       |
| --------------------------------------- | --------------------------------- |
| `/die`                                  | Instantly die when knocked out    |
| `/carry`                                | Carry a nearby knocked-out player |
| `/drop`                                 | Drop the carried player           |
| `/knockout revive <player>`             | Revive a specific player          |
| `/knockout knockout <player> <seconds>` | Knockout a player for a set time  |
| `/knockout reload`                      | Reload the plugin configuration   |


</details>

---

<details>
  <summary>🔐 <strong>Permissions</strong></summary>

| Permission         | Description                    |
| ------------------ | ------------------------------ |
| `knockout.carry`   | Use `/carry`                   |
| `knockout.drop`    | Use `/drop`                    |
| `knockout.die`     | Use `/die`                     |
| `knockout.revive`  | Revive players                 |
| `knockout.command` | Use commands while knocked out |
| `knockout.admin`   | Use admin `/knockout` commands |


</details>

---

<details>
  <summary>📦 <strong>PlaceholderAPI placeholders</strong></summary>

| Placeholder                   | Description                                    |
| ----------------------------- | ---------------------------------------------- |
| `%knockout_knocked_out%`      | `TRUE` if player is knocked out                |
| `%knockout_time_left%`        | Remaining knockout time                        |
| `%knockout_killer%`           | Name of entity that knocked the player out     |
| `%knockout_vehicle%`          | Name of player carrying the knocked-out player |
| `%knockout_is_being_revived%` | `TRUE` if currently being revived              |
| `%knockout_knockouts%`        | Number of knocked-out players on the server    |


</details>

---
