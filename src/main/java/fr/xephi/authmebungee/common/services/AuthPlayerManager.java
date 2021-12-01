package fr.xephi.authmebungee.common.services;

import fr.xephi.authmebungee.bungeecord.data.BungeeAuthPlayer;
import fr.xephi.authmebungee.common.data.AuthPlayer;

import java.util.HashMap;
import java.util.Map;

/*
 * Players manager - store all references to AuthPlayer objects through an HashMap
 */
public class AuthPlayerManager<T> {

    protected Map<String, AuthPlayer<T>> players;

    public AuthPlayerManager() {
        players = new HashMap<>();
    }

    public void addAuthPlayer(AuthPlayer<T> player) {
        players.put(player.getName(), player);
    }

    public void removeAuthPlayer(String name) {
        players.remove(name.toLowerCase());
    }

    public AuthPlayer<T> getAuthPlayer(String name) {
        return players.get(name.toLowerCase());
    }
}
