package fr.xephi.authmebungee.velocity.listeners;

import ch.jalu.configme.SettingsManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.xephi.authmebungee.velocity.ConsoleLogger;
import fr.xephi.authmebungee.common.config.ProxyConfigProperties;
import fr.xephi.authmebungee.common.config.SettingsDependent;
import fr.xephi.authmebungee.common.data.AuthPlayer;
import fr.xephi.authmebungee.common.listeners.MessageListener;
import fr.xephi.authmebungee.velocity.output.ConsoleLoggerFactory;
import fr.xephi.authmebungee.velocity.services.VelocityAuthPlayerManager;

import javax.inject.Inject;
import java.util.Optional;

public class VelocityMessageListener extends MessageListener implements SettingsDependent {

    private final ProxyServer server;

    // Services
    private final ConsoleLogger logger;

    @Inject
    public VelocityMessageListener(final SettingsManager settings, final VelocityAuthPlayerManager velocityAuthPlayerManager, final ProxyServer server) {
        super(settings, velocityAuthPlayerManager, ConsoleLoggerFactory.get(VelocityPlayerListener.class));
        this.server = server;
        this.logger = ConsoleLoggerFactory.get(VelocityPlayerListener.class);
        reload(settings);
    }

    @Override
    public void reload(final SettingsManager settings) {
        isSendOnLogoutEnabled = settings.getProperty(ProxyConfigProperties.ENABLE_SEND_ON_LOGOUT);
        sendOnLogoutTarget = settings.getProperty(ProxyConfigProperties.SEND_ON_LOGOUT_TARGET);
    }

    @Subscribe
    public void onPluginMessage(final PluginMessageEvent event) {
        if (!event.getResult().isAllowed()) {
            return;
        }

        // Check if the message is for a server (ignore client messages)
        if (!event.getIdentifier().getId().equals("authme:main")) {
            return;
        }

        // Check if a player is not trying to send us a fake message
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        // Read the plugin message
        final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        handleMessage(in);
    }

    @Override
    public void sendOnLogout(AuthPlayer<?> player) {
        final Player velocityPlayer = (Player) player.getPlayer();
        if (velocityPlayer != null) {
            Optional<RegisteredServer> registeredServer = server.getServer(sendOnLogoutTarget);
            registeredServer.ifPresent(velocityPlayer::createConnectionRequest);
        }
    }

    @Override
    public void sendToServer(AuthPlayer<?> player, String target) {
        final Player velocityPlayer = (Player) player.getPlayer();
        if (velocityPlayer != null) {
            Optional<RegisteredServer> registeredServer = server.getServer(target);
            registeredServer.ifPresent(velocityPlayer::createConnectionRequest);
        }
    }

}
