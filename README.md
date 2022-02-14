# AuthMeVelocity

This plugin acts like a bridge between your bukkit servers and your Velocity instance. To explain simple how it works, bukkit-side plugins send a message to velocity-side on user authentication. If velocity-side doesn't receive this message, the player won't be able to talk in chat and to perform commands, including Velocity commands.

**AutoLogin:** If you have AuthMe on multiple servers, you can enable autoLogin option that allow you to switch between your servers without having to perform login command again.

## Support
- [GitHub issue tracker](https://github.com/iAverages/AuthMeVelocity/issues)

## Requirements

- Java 1.8+
- Velocity 3.0.0+
- CraftBukkit/Spigot/Paper/Tacospigot 1.7.10+

## Installation

1. Download AuthMeVelocity package
2. Place AuthMeVelocity.jar into your Velocity's plugin folder
4. Restart everything
5. Configure the plugin **(don't forget to config authServers)**
6. Enable the **Hooks.bungeecord** option in your **AuthMeReloaded config file**
7. Enjoy!

**Please follow these steps and configure the plugin before saying it doesn't work!**
