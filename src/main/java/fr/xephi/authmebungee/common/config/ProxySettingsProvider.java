package fr.xephi.authmebungee.common.config;

import fr.xephi.authmebungee.common.annotations.DataFolder;

import javax.inject.Inject;
import java.io.File;

public class ProxySettingsProvider extends SettingsProvider {

    @Inject
    public ProxySettingsProvider(@DataFolder File dataFolder) {
        super(dataFolder, ProxyConfigProperties.class);
    }

}
