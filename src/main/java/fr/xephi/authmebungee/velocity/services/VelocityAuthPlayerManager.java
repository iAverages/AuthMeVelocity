package fr.xephi.authmebungee.velocity.services;

import com.velocitypowered.api.proxy.Player;
import fr.xephi.authmebungee.common.services.AuthPlayerManager;
import fr.xephi.authmebungee.velocity.data.VelocityAuthPlayer;
import fr.xephi.authmebungee.velocity.data.AuthPlayerFactory;

import javax.inject.Inject;

public class VelocityAuthPlayerManager extends AuthPlayerManager<Player> {

    private final AuthPlayerFactory authPlayerFactory;

    @Inject
    public VelocityAuthPlayerManager(AuthPlayerFactory authPlayerFactory) {
        super();
        this.authPlayerFactory = authPlayerFactory;
    }

    public void addAuthPlayer(Player player) {
        addAuthPlayer(authPlayerFactory.getAuthPlayer(player.getUsername().toLowerCase()));
    }

    public void removeAuthPlayer(Player player) {
        removeAuthPlayer(player.getUsername());
    }

    public VelocityAuthPlayer getAuthPlayer(Player player) {
        return (VelocityAuthPlayer) getAuthPlayer(player.getUsername());
    }
}
