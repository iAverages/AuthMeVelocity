package fr.xephi.authmebungee.common.listeners;

import ch.jalu.configme.SettingsManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import fr.xephi.authmebungee.common.utils.GenericLogger;
import fr.xephi.authmebungee.common.config.ProxyConfigProperties;
import fr.xephi.authmebungee.common.config.SettingsDependent;
import fr.xephi.authmebungee.common.data.AuthPlayer;
import fr.xephi.authmebungee.common.services.AuthPlayerManager;

public abstract class MessageListener implements SettingsDependent {

    // Services
    private final AuthPlayerManager<?> authPlayerManager;
    private final GenericLogger logger;

    // Settings
    protected boolean isSendOnLogoutEnabled;
    protected String sendOnLogoutTarget;
    protected boolean isVelocitySupportEnabled;

    public MessageListener(final SettingsManager settings, final AuthPlayerManager<?> authPlayerManager, GenericLogger logger) {
        this.authPlayerManager = authPlayerManager;
        this.logger = logger;
        reload(settings);
    }

    @Override
    public void reload(final SettingsManager settings) {
        isSendOnLogoutEnabled = settings.getProperty(ProxyConfigProperties.ENABLE_SEND_ON_LOGOUT);
        sendOnLogoutTarget = settings.getProperty(ProxyConfigProperties.SEND_ON_LOGOUT_TARGET);
        isVelocitySupportEnabled = settings.getProperty(ProxyConfigProperties.VELOCITY_MESSAGING);
    }

    protected void handleMessage(ByteArrayDataInput in) {
        // Accept only broadcasts
        String channel = in.readUTF();

        //Check if valid channel
        if (isVelocitySupportEnabled) {
            if(!channel.equals("Forward") && !channel.equals("ConnectOther")) return;
        } else {
            if(!channel.equals("Forward")) return;
        }

        //Handles ConnectOther
        if (isVelocitySupportEnabled) {
            if(channel.equals("ConnectOther")) {
                logger.debug("Received plugin message on " + channel);
                handleConnectOther(in);
                return;
            }
        }

        in.readUTF(); // Skip ONLINE/ALL parameter

        // Let's check the subchannel
        String subChannel = in.readUTF();

        if (!subChannel.equals("AuthMe.v2.Broadcast")) {
            return;
        }

        // Read data byte array
        final short dataLength = in.readShort();
        final byte[] dataBytes = new byte[dataLength];
        in.readFully(dataBytes);
        final ByteArrayDataInput dataIn = ByteStreams.newDataInput(dataBytes);

        // For now that's the only type of message the server is able to receive
        final String type = dataIn.readUTF();
        logger.debug("Received plugin message on " + subChannel + " type " + type);
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
        final AuthPlayer<?> bungeeAuthPlayer = authPlayerManager.getAuthPlayer(name);
        if (bungeeAuthPlayer != null) {
            bungeeAuthPlayer.setLogged(true);
        }
    }

    private void handleOnLogout(final ByteArrayDataInput in) {
        final String name = in.readUTF();
        final AuthPlayer<?> authPlayer = authPlayerManager.getAuthPlayer(name);
        if (authPlayer != null) {
            authPlayer.setLogged(false);
            if (isSendOnLogoutEnabled) {
                sendOnLogout(authPlayer);
            }
        }
    }

    private void handleConnectOther(final ByteArrayDataInput in) {
        final String name = in.readUTF();
        final String server = in.readUTF();
        final AuthPlayer<?> authPlayer = authPlayerManager.getAuthPlayer(name);
        if (authPlayer != null) {
            authPlayer.setLogged(false);
            if (isSendOnLogoutEnabled) {
                sendToServer(authPlayer, server);
            }
        }
    }

    public abstract void sendOnLogout(AuthPlayer<?> player);

    public abstract void sendToServer(AuthPlayer<?> player, String target);
}
