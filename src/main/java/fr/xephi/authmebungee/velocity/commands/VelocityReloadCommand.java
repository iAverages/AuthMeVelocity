package fr.xephi.authmebungee.velocity.commands;

import ch.jalu.configme.SettingsManager;
import ch.jalu.injector.factory.SingletonStore;
import com.velocitypowered.api.command.RawCommand;
import fr.xephi.authmebungee.common.config.SettingsDependent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.inject.Inject;

public class VelocityReloadCommand implements RawCommand {

    private SettingsManager settings;
    private SingletonStore<SettingsDependent> settingsDependentStore;

    @Inject
    public VelocityReloadCommand(SettingsManager settings, SingletonStore<SettingsDependent> settingsDependentStore) {
        this.settings = settings;
        this.settingsDependentStore = settingsDependentStore;
    }

    @Override
    public void execute(final Invocation invocation) {
        settings.reload();
        settingsDependentStore.retrieveAllOfType().forEach(settingsDependent -> settingsDependent.reload(settings));
        invocation.source().sendMessage(Component.text("AuthMeBungee configuration reloaded!", NamedTextColor.GREEN));
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("authmebungee.reload");
    }
}
