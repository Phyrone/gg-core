package de.phyrone.gg.embeded;

import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

public class GGCoreLazyLoadUtils {

    private GGCoreLazyLoadUtils() {
    }

    static void lazyLoadGGCore() throws IOException, InvalidDescriptionException, InvalidPluginException, AssertionError {
        Plugin corePlugin = getCorePlugin();
        if (corePlugin == null) {
            File corePluginFile = new File("plugins/", "GG-Core.jar");
            if (!corePluginFile.exists()) {
                downloadCore(corePluginFile);
            }
            corePlugin = loadCore(corePluginFile);
        }
        assert corePlugin != null;
        enableIfNotHappen(corePlugin);

    }

    static private Plugin loadCore(File coreFile) throws InvalidDescriptionException, InvalidPluginException {
        return Bukkit.getPluginManager().loadPlugin(coreFile);
    }

    @Nullable
    static public Plugin getCorePlugin() {
        return Bukkit.getPluginManager().getPlugin("GG-Core");
    }

    static private void enableIfNotHappen(@NotNull Plugin plugin) {
        if (!Bukkit.getPluginManager().isPluginEnabled(plugin)) {
            Bukkit.getPluginManager().enablePlugin(plugin);
        }
    }

    static private void downloadCore(@NotNull File downloadFile) throws IOException {
        URL downloadURL = new URL(Const.GG_CORE_DOWNLOAD_LINK);
        URLConnection connection = downloadURL.openConnection();
        InputStream inputStream = connection.getInputStream();
        Files.copy(inputStream, downloadFile.toPath());
    }
}
