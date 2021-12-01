package fr.xephi.authmebungee.bungeecord.services;

import fr.xephi.authmebungee.bungeecord.data.BungeeAuthPlayer;
import fr.xephi.authmebungee.common.services.AuthPlayerManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeAuthPlayerManager extends AuthPlayerManager<ProxiedPlayer> {

    public void addAuthPlayer(ProxiedPlayer player) {
        addAuthPlayer(new BungeeAuthPlayer(player.getName().toLowerCase()));
    }

    public void removeAuthPlayer(ProxiedPlayer player) {
        removeAuthPlayer(player.getName());
    }

    public BungeeAuthPlayer getAuthPlayer(ProxiedPlayer player) {
        return (BungeeAuthPlayer) getAuthPlayer(player.getName());
    }
}
