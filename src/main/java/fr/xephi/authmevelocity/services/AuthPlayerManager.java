package fr.xephi.authmevelocity.services;

import com.velocitypowered.api.proxy.Player;
import fr.xephi.authmevelocity.data.AuthPlayer;

import java.util.HashMap;
import java.util.Map;

/*
 * Players manager - store all references to AuthPlayer objects through an HashMap
 */
public class AuthPlayerManager {

    private final Map<String, AuthPlayer> players;

    public AuthPlayerManager() {
        players = new HashMap<>();
    }

    public void addAuthPlayer(AuthPlayer player) {
        players.put(player.getName(), player);
    }

    public void addAuthPlayer(Player player) {
        addAuthPlayer(new AuthPlayer(player.getUsername().toLowerCase()));
    }

    public void removeAuthPlayer(String name) {
        players.remove(name.toLowerCase());
    }

    public void removeAuthPlayer(Player player) {
        removeAuthPlayer(player.getUsername());
    }

    public AuthPlayer getAuthPlayer(String name) {
        return players.get(name.toLowerCase());
    }

    public AuthPlayer getAuthPlayer(Player player) {
        return getAuthPlayer(player.getUsername());
    }
}
