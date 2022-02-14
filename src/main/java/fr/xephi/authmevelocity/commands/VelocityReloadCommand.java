package fr.xephi.authmevelocity.commands;

import ch.jalu.configme.SettingsManager;
import ch.jalu.injector.factory.SingletonStore;
import com.velocitypowered.api.command.RawCommand;
import fr.xephi.authmevelocity.config.SettingsDependent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.inject.Inject;

public class VelocityReloadCommand implements RawCommand {

    private final SettingsManager settings;
    private final SingletonStore<SettingsDependent> settingsDependentStore;

    @Inject
    public VelocityReloadCommand(SettingsManager settings, SingletonStore<SettingsDependent> settingsDependentStore) {
        this.settings = settings;
        this.settingsDependentStore = settingsDependentStore;
    }

    @Override
    public void execute(RawCommand.Invocation invocation) {
        settings.reload();
        settingsDependentStore.retrieveAllOfType().forEach(settingsDependent -> settingsDependent.reload(settings));
        invocation.source().sendMessage(Component.text("AuthMeVelocity configuration reloaded!").color(NamedTextColor.GREEN));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("authmevelocity.reload");
    }
}
