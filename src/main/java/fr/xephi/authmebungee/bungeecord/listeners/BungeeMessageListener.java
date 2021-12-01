package fr.xephi.authmebungee.bungeecord.listeners;

import ch.jalu.configme.SettingsManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import fr.xephi.authmebungee.common.config.ProxyConfigProperties;
import fr.xephi.authmebungee.bungeecord.services.BungeeAuthPlayerManager;
import fr.xephi.authmebungee.common.data.AuthPlayer;
import fr.xephi.authmebungee.common.listeners.MessageListener;
import fr.xephi.authmebungee.common.utils.GenericLogger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.inject.Inject;

public class BungeeMessageListener extends MessageListener implements Listener  {

    private final ProxyServer server;
    private final GenericLogger genericLogger;
    // Settings

    @Inject
    public BungeeMessageListener(final SettingsManager settings, final BungeeAuthPlayerManager bungeeAuthPlayerManager, ProxyServer server, final GenericLogger genericLogger) {
        super(settings, bungeeAuthPlayerManager, genericLogger);
        this.server = server;
        this.genericLogger = genericLogger;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // Check if the message is for a server (ignore client messages)
        if (isVelocitySupportEnabled) {
            if (!event.getTag().equals("authme:main")) return;
        } else {
            if (!event.getTag().equals("BungeeCord")) return;
        }

        // Check if a player is not trying to send us a fake message
        if (!(event.getSender() instanceof Server)) {
            return;
        }

        // Read the plugin message
        final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        handleMessage(in);
    }

    @Override
    public void sendOnLogout(AuthPlayer<?> player) {
        final ProxiedPlayer proxiedPlayer = (ProxiedPlayer) player.getPlayer();
        if (proxiedPlayer != null) {
            proxiedPlayer.connect(ProxyServer.getInstance().getServerInfo(sendOnLogoutTarget));
        }
    }

    @Override
    public void sendToServer(AuthPlayer<?> player, String target) {
        final ProxiedPlayer proxiedPlayer = (ProxiedPlayer) player.getPlayer();
        if (proxiedPlayer != null) {
            proxiedPlayer.connect(server.getServerInfo(target));
        }
    }

}
