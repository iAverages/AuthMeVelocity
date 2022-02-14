package fr.xephi.authmevelocity;

import ch.jalu.configme.SettingsManager;
import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.xephi.authmevelocity.annotations.DataFolder;
import fr.xephi.authmevelocity.commands.VelocityReloadCommand;
import fr.xephi.authmevelocity.config.VelocityConfigProperties;
import fr.xephi.authmevelocity.config.VelocitySettingsProvider;
import fr.xephi.authmevelocity.listeners.VelocityMessageListener;
import fr.xephi.authmevelocity.listeners.VelocityPlayerListener;
import fr.xephi.authmevelocity.services.AuthPlayerManager;
import fr.xephi.authmevelocity.utils.PluginChannels;

import java.io.File;
import java.util.logging.Logger;

@Plugin(id = "authmevelocity", name = "AuthMeVelocity", version = "2.2.0-SNAPSHOT",
    description = "Velocity addon for AuthMe!", authors = {"AuthMeTeam", "dan <danielraybone.com>"})
public class AuthMeVelocity {

    private static AuthMeVelocity instance;
    private final ProxyServer proxy;
    private final Logger logger;

    // Instances
    private Injector injector;
    private SettingsManager settings;
    private AuthPlayerManager authPlayerManager;

    @Inject
    public AuthMeVelocity(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;

        instance = this;
    }

    public static AuthMeVelocity getInstance() {
        return instance;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        // Prepare the injector and register stuff
        setupInjector();

        // Get singletons from the injector
        settings = injector.getSingleton(SettingsManager.class);
        authPlayerManager = injector.getSingleton(AuthPlayerManager.class);

        // Print some config information
        logger.info("Current auth servers:");
        for (String authServer : settings.getProperty(VelocityConfigProperties.AUTH_SERVERS)) {
            logger.info("> " + authServer.toLowerCase());
        }

        // Add online players (plugin hotswap, just in case)
        for (Player player : proxy.getAllPlayers()) {
            authPlayerManager.addAuthPlayer(player);
        }

        // Register commands
        CommandMeta meta = proxy.getCommandManager().metaBuilder("abreloadproxy").build();
        proxy.getCommandManager().register(meta, injector.getSingleton(VelocityReloadCommand.class));

        // Registering event listeners
        proxy.getChannelRegistrar().register(PluginChannels.LEGACY_BUNGEE_CHANNEL, PluginChannels.MODERN_BUNGEE_CHANNEL);
        proxy.getEventManager().register(this, injector.getSingleton(VelocityMessageListener.class));
        proxy.getEventManager().register(this, injector.getSingleton(VelocityPlayerListener.class));
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    private void setupInjector() {
        // Setup injector
        injector = new InjectorBuilder().addDefaultHandlers("").create();
        injector.register(Logger.class, logger);
        injector.register(AuthMeVelocity.class, this);
        injector.register(ProxyServer.class, proxy);
        injector.provide(DataFolder.class, new File("./plugins/AuthMeVelocity/"));
        injector.registerProvider(SettingsManager.class, VelocitySettingsProvider.class);
    }

}
