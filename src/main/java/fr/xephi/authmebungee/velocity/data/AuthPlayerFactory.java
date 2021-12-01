package fr.xephi.authmebungee.velocity.data;


import com.velocitypowered.api.proxy.ProxyServer;

import javax.inject.Inject;

public class AuthPlayerFactory {

    private ProxyServer server;

    @Inject
    public AuthPlayerFactory(ProxyServer server) {
        this.server = server;
    }

    public VelocityAuthPlayer getAuthPlayer(String name) {
        return new VelocityAuthPlayer(server, name);
    }

    public VelocityAuthPlayer getAuthPlayer(String name, boolean isLogged) {
        return new VelocityAuthPlayer(server, name, isLogged);
    }
}
