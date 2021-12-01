package fr.xephi.authmebungee.bungeecord;

import ch.jalu.configme.SettingsManager;
import fr.xephi.authmebungee.common.config.ProxyConfigProperties;
import fr.xephi.authmebungee.common.config.SettingsDependent;
import fr.xephi.authmebungee.common.utils.GenericLogger;
import fr.xephi.authmebungee.velocity.output.LogLevel;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class BungeeLogger implements GenericLogger, SettingsDependent {

    private final Logger logger;
    private final SettingsManager settings;
    private LogLevel logLevel;

    @Inject
    public BungeeLogger(Logger logger, SettingsManager settings) {
        this.logger = logger;
        this.settings = settings;
        reload(settings);
    }

    @Override
    public void warning(String message) {
        logger.warning(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void fine(String message) {
        logger.fine(message);
    }

    @Override
    public void debug(String message) {
        logAndWriteWithDebugPrefix(message);
    }

    @Override
    public void debug(String message, Object param1) {
        debug(message, new Object[]{param1});
    }

    @Override
    public void debug(String message, Object param1, Object param2) {
        debug(message, new Object[]{param1, param2});
    }

    @Override
    public void debug(String message, Object... params) {
        logAndWriteWithDebugPrefix(MessageFormat.format(message, params));
    }

    @Override
    public void debug(Supplier<String> msgSupplier) {
        logAndWriteWithDebugPrefix(msgSupplier.get());
    }

    private void logAndWriteWithDebugPrefix(String message) {
        if (logLevel.includes(LogLevel.DEBUG)) {
            String debugMessage = "[DEBUG] " + message;
            logger.info(debugMessage);
        }
    }

    @Override
    public void reload(SettingsManager settings) {
        this.logLevel = LogLevel.valueOf(settings.getProperty(ProxyConfigProperties.LOG_LEVEL));
        if(logLevel.includes(LogLevel.DEBUG)) {
            info("LogLevel.DEBUG");
        }
    }
}
