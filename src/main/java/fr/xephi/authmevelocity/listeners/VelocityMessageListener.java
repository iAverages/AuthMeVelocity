package fr.xephi.authmevelocity.listeners;

import ch.jalu.configme.SettingsManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import fr.xephi.authmevelocity.AuthMeVelocity;
import fr.xephi.authmevelocity.config.VelocityConfigProperties;
import fr.xephi.authmevelocity.config.SettingsDependent;
import fr.xephi.authmevelocity.data.AuthPlayer;
import fr.xephi.authmevelocity.services.AuthPlayerManager;
import fr.xephi.authmevelocity.utils.PluginChannels;

import javax.inject.Inject;
import java.util.Optional;

public class VelocityMessageListener implements SettingsDependent {

    // Services
    private final AuthPlayerManager authPlayerManager;

    // Settings
    private boolean isSendOnLogoutEnabled;
    private String sendOnLogoutTarget;

    @Inject
    public VelocityMessageListener(final SettingsManager settings, final AuthPlayerManager authPlayerManager) {
        this.authPlayerManager = authPlayerManager;
        reload(settings);
    }

    @Override
    public void reload(final SettingsManager settings) {
        isSendOnLogoutEnabled = settings.getProperty(VelocityConfigProperties.ENABLE_SEND_ON_LOGOUT);
        sendOnLogoutTarget = settings.getProperty(VelocityConfigProperties.SEND_ON_LOGOUT_TARGET);
    }

    @Subscribe
    public void onPluginMessage(final PluginMessageEvent event) {
        // Check if the message is for a server (ignore client messages)
        if (!event.getIdentifier().equals(PluginChannels.MODERN_BUNGEE_CHANNEL) &&
            !event.getIdentifier().equals(PluginChannels.LEGACY_BUNGEE_CHANNEL)) {
            return;
        }

        // Check if a player is not trying to send us a fake message
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        // Read the plugin message
        final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

        // Accept only broadcasts
        if (!in.readUTF().equals("Forward")) {
            return;
        }
        in.readUTF(); // Skip ONLINE/ALL parameter

        // Let's check the subchannel
        if (!in.readUTF().equals("AuthMe.v2.Broadcast")) {
            return;
        }

        // Read data byte array
        final short dataLength = in.readShort();
        final byte[] dataBytes = new byte[dataLength];
        in.readFully(dataBytes);
        final ByteArrayDataInput dataIn = ByteStreams.newDataInput(dataBytes);

        // For now that's the only type of message the server is able to receive
        final String type = dataIn.readUTF();
        switch (type) {
            case "login":
                handleOnLogin(dataIn);
                break;
            case "logout":
            case "unregister":
                handleOnLogout(dataIn);
                break;
        }
    }

    private void handleOnLogin(final ByteArrayDataInput in) {
        final String name = in.readUTF();
        final AuthPlayer authPlayer = authPlayerManager.getAuthPlayer(name);
        if (authPlayer != null) {
            authPlayer.setLogged(true);
        }
    }

    private void handleOnLogout(final ByteArrayDataInput in) {
        final String name = in.readUTF();
        final AuthPlayer authPlayer = authPlayerManager.getAuthPlayer(name);
        if (authPlayer == null) {
            return;
        }
        authPlayer.setLogged(false);
        if (isSendOnLogoutEnabled) {
            Optional<Player> player = authPlayer.getPlayer();
            player.ifPresent(p ->
                AuthMeVelocity.getInstance().getProxy().getServer(sendOnLogoutTarget)
                    .ifPresent(server -> p.createConnectionRequest(server).connect()));
        }
    }
}
