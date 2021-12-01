package fr.xephi.authmebungee.bungeecord.listeners;

import ch.jalu.configme.SettingsManager;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.xephi.authmebungee.bungeecord.AuthMeBungee;
import fr.xephi.authmebungee.common.config.ProxyConfigProperties;
import fr.xephi.authmebungee.common.config.SettingsDependent;
import fr.xephi.authmebungee.bungeecord.data.BungeeAuthPlayer;
import fr.xephi.authmebungee.bungeecord.services.BungeeAuthPlayerManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class BungeePlayerListener implements Listener, SettingsDependent {

    // Services
    private final BungeeAuthPlayerManager bungeeAuthPlayerManager;
    private final AuthMeBungee authMeBungee;

    // Settings
    private boolean isAutoLoginEnabled;
    private boolean isServerSwitchRequiresAuth;
    private String requiresAuthKickMessage;
    private List<String> authServers;
    private boolean allServersAreAuthServers;
    private boolean isCommandsRequireAuth;
    private List<String> commandWhitelist;
    private boolean chatRequiresAuth;
    private boolean isVelocitySupportEnabled;

    private String pluginChannel;

    @Inject
    public BungeePlayerListener(final SettingsManager settings, final BungeeAuthPlayerManager bungeeAuthPlayerManager, AuthMeBungee authMeBungee) {
        this.bungeeAuthPlayerManager = bungeeAuthPlayerManager;
        this.authMeBungee = authMeBungee;
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


        isVelocitySupportEnabled = settings.getProperty(ProxyConfigProperties.VELOCITY_MESSAGING);
        if(isVelocitySupportEnabled) {
            pluginChannel = "authme:main";
        } else {
            pluginChannel = "BungeeCord";
        }
    }

    @EventHandler
    public void onPlayerJoin(final PostLoginEvent event) {
        // Register player in our list
        bungeeAuthPlayerManager.addAuthPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDisconnect(final PlayerDisconnectEvent event) {
        // Remove player from out list
        bungeeAuthPlayerManager.removeAuthPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(final ChatEvent event) {
        if (event.isCancelled() || !event.isCommand() || !isCommandsRequireAuth) {
            return;
        }

        // Check if it's a player
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        // Filter only unauthenticated players
        final BungeeAuthPlayer bungeeAuthPlayer = bungeeAuthPlayerManager.getAuthPlayer(player);
        if (bungeeAuthPlayer != null && bungeeAuthPlayer.isLogged()) {
            return;
        }
        // Only in auth servers
        if (!isAuthServer(player.getServer().getInfo())) {
            return;
        }
        // Check if command is whitelisted command
        if (commandWhitelist.contains(event.getMessage().split(" ")[0].toLowerCase())) {
            return;
        }
        event.setCancelled(true);
    }

    // Priority is set to lowest to keep compatibility with some chat plugins
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(final ChatEvent event) {
        if (event.isCancelled() || event.isCommand()) {
            return;
        }

        // Check if it's a player
        if (!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        final ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        // Filter only unauthenticated players
        final BungeeAuthPlayer bungeeAuthPlayer = bungeeAuthPlayerManager.getAuthPlayer(player);
        if (bungeeAuthPlayer != null && bungeeAuthPlayer.isLogged()) {
            return;
        }
        // Only in auth servers
        if (!isAuthServer(player.getServer().getInfo())) {
            return;
        }

        if (!chatRequiresAuth) {
            return;
        }
        event.setCancelled(true);
    }

    private boolean isAuthServer(ServerInfo serverInfo) {
        return allServersAreAuthServers || authServers.contains(serverInfo.getName().toLowerCase());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerConnectedToServer(final ServerSwitchEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        final ServerInfo server = player.getServer().getInfo();
        final BungeeAuthPlayer bungeeAuthPlayer = bungeeAuthPlayerManager.getAuthPlayer(player);
        final boolean isAuthenticated = bungeeAuthPlayer != null && bungeeAuthPlayer.isLogged();

        if (isAuthenticated && isAuthServer(server)) {
            // If AutoLogin enabled, notify the server
            if (isAutoLoginEnabled) {
                final ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("AuthMe.v2");
                if (isVelocitySupportEnabled) out.writeUTF(authMeBungee.getInstanceUUID().toString());
                out.writeUTF("perform.login");
                out.writeUTF(event.getPlayer().getName());
                server.sendData(pluginChannel, out.toByteArray(), false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerConnectingToServer(final ServerConnectEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final ProxiedPlayer player = event.getPlayer();
        final BungeeAuthPlayer bungeeAuthPlayer = bungeeAuthPlayerManager.getAuthPlayer(player);
        final boolean isAuthenticated = bungeeAuthPlayer != null && bungeeAuthPlayer.isLogged();

        // Skip logged users
        if (isAuthenticated) {
            return;
        }

        // Only check non auth servers
        if (isAuthServer(event.getTarget())) {
            return;
        }

        // If the player is not logged in and serverSwitchRequiresAuth is enabled, cancel the connection
        if (isServerSwitchRequiresAuth) {
            event.setCancelled(true);

            final TextComponent reasonMessage = new TextComponent(requiresAuthKickMessage);
            reasonMessage.setColor(ChatColor.RED);

            // Handle race condition on player join on a misconfigured network
            if (player.getServer() == null) {
                player.disconnect(reasonMessage);
            } else {
                player.sendMessage(reasonMessage);
            }
        }
    }
}
