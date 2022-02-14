package fr.xephi.authmevelocity.config;

import fr.xephi.authmevelocity.annotations.DataFolder;

import javax.inject.Inject;
import java.io.File;

public class VelocitySettingsProvider extends SettingsProvider {

    @Inject
    public VelocitySettingsProvider(@DataFolder File dataFolder) {
        super(dataFolder, VelocityConfigProperties.class);
    }

}
