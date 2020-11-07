package de.phyrone.gg.embeded;

import de.phyrone.gg.GGCore;
import de.phyrone.gg.module.ApiModuleManager;
import de.phyrone.gg.module.GGModule;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public abstract class GGPlugin extends JavaPlugin {

    protected void enable() {
    }

    protected void disable() {
    }

    @Override
    final public void onEnable() {
        try {
            GGCoreLazyLoadUtils.lazyLoadGGCore();
        } catch (IOException | InvalidDescriptionException | InvalidPluginException | AssertionError e) {
            getLogger().log(Level.SEVERE, "Looks like we are unable to get GG-Core --> plugin " + getName() + " could not be activated!", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        ApiModuleManager moduleManager = GGCore.getInstance(this).getModuleManager();
        moduleManager.getModuleHandler(this::getModules);
        moduleManager.onEnable();
        enable();
    }


    @NotNull
    protected abstract List<Class<? extends GGModule>> getModules();

    @Override
    final public void onDisable() {
        Plugin corePlugin = GGCoreLazyLoadUtils.getCorePlugin();
        assert corePlugin != null;
        if (!corePlugin.isEnabled()) {
            Bukkit.getPluginManager().enablePlugin(corePlugin);
            disableSteps();
            Bukkit.getPluginManager().disablePlugin(corePlugin);
        } else {
            disableSteps();
        }
    }

    private void disableSteps() {
        GGCore.getInstance(this).getModuleManager().onDisable();
        disable();
    }




}
