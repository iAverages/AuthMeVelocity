package fr.xephi.authmebungee.bungeecord.data;

import fr.xephi.authmebungee.common.data.AuthPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeAuthPlayer extends AuthPlayer<ProxiedPlayer> {

    public BungeeAuthPlayer(String name) {
        super(name);
    }

    public ProxiedPlayer getPlayer() {
        for (ProxiedPlayer current : ProxyServer.getInstance().getPlayers()) {
            if (current.getName().equalsIgnoreCase(this.getName())) {
                return current;
            }
        }
        return null;
    }
}
