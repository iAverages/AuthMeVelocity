package fr.xephi.authmebungee.velocity.listeners;

import ch.jalu.configme.SettingsManager;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import fr.xephi.authmebungee.common.config.ProxyConfigProperties;
import fr.xephi.authmebungee.common.config.SettingsDependent;
import fr.xephi.authmebungee.velocity.data.VelocityAuthPlayer;
import fr.xephi.authmebungee.velocity.services.VelocityAuthPlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VelocityPlayerListener implements SettingsDependent {

    // Services
    private final VelocityAuthPlayerManager velocityAuthPlayerManager;

    // Settings
    private boolean isAutoLoginEnabled;
    private boolean isServerSwitchRequiresAuth;
    private String requiresAuthKickMessage;
    private List<String> authServers;
    private boolean allServersAreAuthServers;
    private boolean isCommandsRequireAuth;
    private List<String> commandWhitelist;
    private boolean chatRequiresAuth;

    @Inject
    public VelocityPlayerListener(final SettingsManager settings, final VelocityAuthPlayerManager velocityAuthPlayerManager) {
        this.velocityAuthPlayerManager = velocityAuthPlayerManager;
        reload(settings);
    }

    @Override
    public void reload(final SettingsManager settings) {
        isAutoLoginEnabled = settings.getProperty(ProxyConfigProperties.AUTOLOGIN);
        isServerSwitchRequiresAuth = settings.getProperty(ProxyConfigProperties.SERVER_SWITCH_REQUIRES_AUTH);
        requiresAuthKickMessage = settings.getProperty(ProxyConfigProperties.SERVER_SWITCH_KICK_MESSAGE);
        authServers = new ArrayList<>();
        for (final String server : settings.getProperty(ProxyConfigProperties.AUTH_SERVERS)) {
            authServers.add(server.toLowerCase());
        }
        allServersAreAuthServers = settings.getProperty(ProxyConfigProperties.ALL_SERVERS_ARE_AUTH_SERVERS);
        isCommandsRequireAuth = settings.getProperty(ProxyConfigProperties.COMMANDS_REQUIRE_AUTH);
        commandWhitelist = new ArrayList<>();
        for (final String command : settings.getProperty(ProxyConfigProperties.COMMANDS_WHITELIST)) {
            commandWhitelist.add(command.toLowerCase());
        }
        chatRequiresAuth = settings.getProperty(ProxyConfigProperties.CHAT_REQUIRES_AUTH);
    }

    @Subscribe
    public void onPlayerJoin(final PostLoginEvent event) {
        // Register player in our list
        velocityAuthPlayerManager.addAuthPlayer(event.getPlayer());
    }

    @Subscribe
    public void onPlayerDisconnect(final DisconnectEvent event) {
        // Remove player from out list
        velocityAuthPlayerManager.removeAuthPlayer(event.getPlayer());
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onCommand(final CommandExecuteEvent event) {
        if (!event.getResult().isAllowed() || !isCommandsRequireAuth) {
            return;
        }

        // Check if it's a player
        if (!(event.getCommandSource() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getCommandSource();

        // Filter only unauthenticated players
        final VelocityAuthPlayer velocityAuthPlayer = velocityAuthPlayerManager.getAuthPlayer(player);
        if (velocityAuthPlayer != null && velocityAuthPlayer.isLogged()) {
            return;
        }

        // Only in auth servers
        Optional<ServerConnection> serverConnection = player.getCurrentServer();
        if (serverConnection.isPresent() && !isAuthServer(serverConnection.get().getServerInfo())) {
            return;
        }
        // Check if command is whitelisted command
        if (commandWhitelist.contains("/" + event.getCommand().split(" ")[0].toLowerCase())) {
            return;
        }
        event.setResult(CommandExecuteEvent.CommandResult.denied());
    }

    // Priority is set to lowest to keep compatibility with some chat plugins
    @Subscribe(order = PostOrder.EARLY)
    public void onPlayerChat(final PlayerChatEvent event) {
        if (!event.getResult().isAllowed()) {
            return;
        }

        final Player player = event.getPlayer();

        // Filter only unauthenticated players
        final VelocityAuthPlayer velocityAuthPlayer = velocityAuthPlayerManager.getAuthPlayer(player);
        if (velocityAuthPlayer != null && velocityAuthPlayer.isLogged()) {
            return;
        }
        // Only in auth servers
        Optional<ServerConnection> serverConnection = player.getCurrentServer();
        if (serverConnection.isPresent() && !isAuthServer(serverConnection.get().getServerInfo())) {
            return;
        }

        if (!chatRequiresAuth) {
            return;
        }
        event.setResult(PlayerChatEvent.ChatResult.denied());
    }

    @Subscribe(order =  PostOrder.EARLY)
    public void onPlayerConnectedToServer(final ServerConnectedEvent event) {
        if(event.getPreviousServer().isPresent()) {
            final Player player = event.getPlayer();
            final RegisteredServer server = event.getServer();
            final VelocityAuthPlayer velocityAuthPlayer = velocityAuthPlayerManager.getAuthPlayer(player);
            final boolean isAuthenticated = velocityAuthPlayer != null && velocityAuthPlayer.isLogged();

            if (isAuthenticated && isAuthServer(server.getServerInfo())) {
                // If AutoLogin enabled, notify the server
                if (isAutoLoginEnabled) {
                    final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("AuthMe.v2");
                    out.writeUTF("perform.login");
                    out.writeUTF(event.getPlayer().getUsername());
                    server.sendPluginMessage(new LegacyChannelIdentifier("authme:main"), out.toByteArray());
                }
            }
        }
    }

    @Subscribe(order = PostOrder.LATE)
    public void onPlayerConnectingToServer(final ServerPreConnectEvent event) {
        if (!event.getResult().isAllowed()) {
            return;
        }

        final Player player = event.getPlayer();
        final VelocityAuthPlayer velocityAuthPlayer = velocityAuthPlayerManager.getAuthPlayer(player);
        final boolean isAuthenticated = velocityAuthPlayer != null && velocityAuthPlayer.isLogged();

        // Skip logged users
        if (isAuthenticated) {
            return;
        }

        // Only check non auth servers
        Optional<RegisteredServer> registeredServer = event.getResult().getServer();
        if (registeredServer.isPresent() && isAuthServer(registeredServer.get().getServerInfo())) {
            return;
        }

        // If the player is not logged in and serverSwitchRequiresAuth is enabled, cancel the connection
        if (isServerSwitchRequiresAuth) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

            final Component reasonMessage = Component.text(requiresAuthKickMessage, NamedTextColor.RED);

            // Handle race condition on player join on a misconfigured network
            Optional<ServerConnection> serverConnection = player.getCurrentServer();
            if (!serverConnection.isPresent()) {
                player.disconnect(reasonMessage);
            } else {
                player.sendMessage(reasonMessage);
            }
        }
    }

    private boolean isAuthServer(ServerInfo serverInfo) {
        return allServersAreAuthServers || authServers.contains(serverInfo.getName().toLowerCase());
    }
}
