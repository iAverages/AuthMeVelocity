package fr.xephi.authmebungee.velocity;

import ch.jalu.configme.SettingsManager;
import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.scheduler.Scheduler;
import fr.xephi.authmebungee.bungeecord.BungeeLogger;
import fr.xephi.authmebungee.common.annotations.DataFolder;
import fr.xephi.authmebungee.common.config.ProxyConfigProperties;
import fr.xephi.authmebungee.common.config.ProxySettingsProvider;
import fr.xephi.authmebungee.common.utils.GenericLogger;
import fr.xephi.authmebungee.common.utils.UUIDFileGenerator;
import fr.xephi.authmebungee.velocity.commands.VelocityReloadCommand;
import fr.xephi.authmebungee.velocity.data.AuthPlayerFactory;
import fr.xephi.authmebungee.velocity.listeners.VelocityMessageListener;
import fr.xephi.authmebungee.velocity.listeners.VelocityPlayerListener;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.xephi.authmebungee.velocity.output.ConsoleLoggerFactory;
import fr.xephi.authmebungee.velocity.services.VelocityAuthPlayerManager;
import org.slf4j.Logger;
import org.bstats.velocity.Metrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Plugin(id = "authmebungee", name = "AuthMeBungee", version = "0.1.0-SNAPSHOT",
    url = "github.com/AuthMe/AuthMeBungee", description = "Velocity companion for AuthMe", authors = {"AuthMeTeam"})
public class AuthMeVelocity {

    // Instances
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;

    private Injector injector;
    private SettingsManager settings;
    private VelocityAuthPlayerManager velocityAuthPlayerManager;

    @Inject
    public AuthMeVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Prepare the injector and register stuff
        setupInjector();

        // Get singletons from the injector
        injector.getSingleton(AuthPlayerFactory.class);
        velocityAuthPlayerManager = injector.getSingleton(VelocityAuthPlayerManager.class);
        settings = injector.getSingleton(SettingsManager.class);

        // Set the Logger instance and log file path
        ConsoleLogger.initialize(logger, new File(String.valueOf(dataDirectory), "authmebungee.log"));
        ConsoleLoggerFactory.reloadSettings(settings);

        injector.register(GenericLogger.class, ConsoleLoggerFactory.get(AuthMeVelocity.class));

        //Generating AuthMeBungee instance UUID
        UUIDFileGenerator uuidFileGenerator = injector.getSingleton(UUIDFileGenerator.class);
        try{
            UUID uuid = uuidFileGenerator.generateInstanceUUID();
            logger.info("AuthMeBungee instance UUID: " + uuid);
        } catch (IOException e) {
            logger.error("Unable to generate AuthMeBungee instance UUID!");
            e.printStackTrace();
        }

        // Print some config information
        logger.info("Current auth servers:");
        for (String authServer : settings.getProperty(ProxyConfigProperties.AUTH_SERVERS)) {
            logger.info("> " + authServer.toLowerCase());
        }

        // Add online players (plugin hotswap, just in case)
        for (Player player : server.getAllPlayers()) {
            velocityAuthPlayerManager.addAuthPlayer(player);
        }

        server.getChannelRegistrar().register(MinecraftChannelIdentifier.from(("authme:main")));

        // Register commands
        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("abreloadproxy").build();
        commandManager.register(meta, injector.getSingleton(VelocityReloadCommand.class));

        // Send metrics data
        metricsFactory.make(this, 1880);

        server.getEventManager().register(this, injector.getSingleton(VelocityMessageListener.class));
        server.getEventManager().register(this, injector.getSingleton(VelocityPlayerListener.class));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        ConsoleLogger.closeFileWriter();
    }

     private void setupInjector() {
        // Setup injector
        injector = new InjectorBuilder().addDefaultHandlers("").create();
        injector.register(Logger.class, logger);
        injector.register(AuthMeVelocity.class, this);
        injector.register(ProxyServer.class, server);
        injector.register(PluginManager.class, server.getPluginManager());
        injector.register(Scheduler.class, server.getScheduler());
        injector.provide(DataFolder.class, dataDirectory.toFile());
        injector.registerProvider(SettingsManager.class, ProxySettingsProvider.class);
    }
}
