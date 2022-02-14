package fr.xephi.authmevelocity.data;

import com.velocitypowered.api.proxy.Player;
import fr.xephi.authmevelocity.AuthMeVelocity;

import java.util.Optional;

public class AuthPlayer {

    private final String name;
    private boolean isLogged;

    public AuthPlayer(String name, boolean isLogged) {
        this.name = name.toLowerCase();
        this.isLogged = isLogged;
    }

    public AuthPlayer(String name) {
        this(name, false);
    }

    public String getName() {
        return name;
    }

    public boolean isLogged() {
        return isLogged;
    }

    public void setLogged(boolean isLogged) {
        this.isLogged = isLogged;
    }

    public Optional<Player> getPlayer() {
        return AuthMeVelocity.getInstance().getProxy().getPlayer(this.name);
    }

    public boolean isOnline() {
        return getPlayer().isPresent();
    }

}
