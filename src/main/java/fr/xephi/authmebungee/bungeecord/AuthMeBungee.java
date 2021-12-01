package fr.xephi.authmebungee.bungeecord;

import ch.jalu.configme.SettingsManager;
import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import fr.xephi.authmebungee.common.annotations.DataFolder;
import fr.xephi.authmebungee.bungeecord.commands.BungeeReloadCommand;
import fr.xephi.authmebungee.common.config.ProxyConfigProperties;
import fr.xephi.authmebungee.common.config.ProxySettingsProvider;
import fr.xephi.authmebungee.bungeecord.listeners.BungeeMessageListener;
import fr.xephi.authmebungee.bungeecord.listeners.BungeePlayerListener;
import fr.xephi.authmebungee.bungeecord.services.BungeeAuthPlayerManager;
import fr.xephi.authmebungee.common.utils.GenericLogger;
import fr.xephi.authmebungee.common.utils.UUIDFileGenerator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.bstats.bungeecord.Metrics;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public class AuthMeBungee extends Plugin {

    // Instances
    private Injector injector;
    private SettingsManager settings;
    private BungeeAuthPlayerManager bungeeAuthPlayerManager;
    private UUID instanceUUID;

    public AuthMeBungee() {
    }

    @Override
    public void onEnable() {
        // Prepare the injector and register stuff
        setupInjector();

        // Get singletons from the injector
        settings = injector.getSingleton(SettingsManager.class);
        bungeeAuthPlayerManager = injector.getSingleton(BungeeAuthPlayerManager.class);

        injector.register(GenericLogger.class, injector.getSingleton(BungeeLogger.class));

        //Generating AuthMeBungee instance UUID
        UUIDFileGenerator uuidFileGenerator = injector.getSingleton(UUIDFileGenerator.class);
        try{
            instanceUUID = uuidFileGenerator.generateInstanceUUID();
            getLogger().info("AuthMeBungee instance UUID: " + instanceUUID);
        } catch (IOException e) {
            getLogger().severe("Unable to generate AuthMeBungee instance UUID!");
            e.printStackTrace();
        }

        // Print some config information
        getLogger().info("Current auth servers:");
        for (String authServer : settings.getProperty(ProxyConfigProperties.AUTH_SERVERS)) {
            getLogger().info("> " + authServer.toLowerCase());
        }

        // Add online players (plugin hotswap, just in case)
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            bungeeAuthPlayerManager.addAuthPlayer(player);
        }

        if(settings.getProperty(ProxyConfigProperties.VELOCITY_MESSAGING)) {
            getProxy().registerChannel("authme:main");
            getLogger().info("Velocity plugin messaging support enabled on channel authme:main");
        }



        // Register commands
        getProxy().getPluginManager().registerCommand(this, injector.getSingleton(BungeeReloadCommand.class));

        // Registering event listeners
        getProxy().getPluginManager().registerListener(this, injector.getSingleton(BungeeMessageListener.class));
        getProxy().getPluginManager().registerListener(this, injector.getSingleton(BungeePlayerListener.class));

        // Send metrics data
        new Metrics(this, 1880);
    }

    private void setupInjector() {
        // Setup injector
        injector = new InjectorBuilder().addDefaultHandlers("").create();
        injector.register(Logger.class, getLogger());
        injector.register(AuthMeBungee.class, this);
        injector.register(ProxyServer.class, getProxy());
        injector.register(PluginManager.class, getProxy().getPluginManager());
        injector.register(TaskScheduler.class, getProxy().getScheduler());
        injector.provide(DataFolder.class, getDataFolder());
        injector.registerProvider(SettingsManager.class, ProxySettingsProvider.class);
    }

    public UUID getInstanceUUID() {
        return instanceUUID;
    }

}
