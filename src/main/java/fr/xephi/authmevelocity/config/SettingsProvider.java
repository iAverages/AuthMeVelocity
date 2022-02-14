package fr.xephi.authmevelocity.config;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;

import javax.inject.Provider;
import java.io.File;

/**
 * Initializes the settings.
 */
public abstract class SettingsProvider implements Provider<SettingsManager> {

    private final File dataFolder;

    private final Class<? extends SettingsHolder> properties;

    protected SettingsProvider(File dataFolder, Class<? extends SettingsHolder> properties) {
        this.dataFolder = dataFolder;
        this.properties = properties;
    }

    /**
     * Loads the plugin's settings.
     *
     * @return the settings instance, or null if it could not be constructed
     */
    @Override
    public SettingsManager get() {
        File configFile = new File(dataFolder, "config.yml");
        return SettingsManagerBuilder.withYamlFile(configFile)
            .useDefaultMigrationService()
            .configurationData(properties)
            .create();
    }
}
