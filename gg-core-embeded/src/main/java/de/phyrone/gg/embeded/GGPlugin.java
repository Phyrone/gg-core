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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
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
            prepareCorePlugin();
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
        Plugin corePlugin = getCorePlugin();
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

    private static final File pluginsFolder = new File("plugins/");

    private void prepareCorePlugin() throws IOException, InvalidDescriptionException, InvalidPluginException, AssertionError {
        Plugin corePlugin = getCorePlugin();
        if (corePlugin == null) {
            File corePluginFile = new File(pluginsFolder, "GG-Core.jar");
            if (!corePluginFile.exists()) {
                downloadCore(corePluginFile);
            }
            corePlugin = loadCore(corePluginFile);
        }
        assert corePlugin != null;
        enableIfNotHappen(corePlugin);

    }

    private Plugin loadCore(File coreFile) throws InvalidDescriptionException, InvalidPluginException {
        return Bukkit.getPluginManager().loadPlugin(coreFile);
    }

    @Nullable
    private Plugin getCorePlugin() {
        return Bukkit.getPluginManager().getPlugin("GG-Core");
    }

    private void enableIfNotHappen(@NotNull Plugin plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled(plugin)) {
            Bukkit.getPluginManager().enablePlugin(plugin);
        }
    }

    private void downloadCore(@NotNull File downloadFile) throws IOException {
        URL downloadURL = new URL(Const.GG_CORE_DOWNLOAD_LINK);
        URLConnection connection = downloadURL.openConnection();
        InputStream inputStream = connection.getInputStream();
        Files.copy(inputStream, downloadFile.toPath());
    }
}
