package fr.xephi.authmebungee.velocity.data;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.xephi.authmebungee.common.data.AuthPlayer;

public class VelocityAuthPlayer extends AuthPlayer<Player> {

    private ProxyServer server;

    public VelocityAuthPlayer(ProxyServer server, String name, boolean isLogged) {
        super(name, isLogged);
        this.server = server;
    }

    public VelocityAuthPlayer(ProxyServer server, String name) {
        super(name);
        this.server = server;
    }

    public Player getPlayer() {
        for (Player current : server.getAllPlayers()) {
            if (current.getUsername().equalsIgnoreCase(this.getName())) {
                return current;
            }
        }
        return null;
    }

}
