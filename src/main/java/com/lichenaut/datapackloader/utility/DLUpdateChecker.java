package com.lichenaut.datapackloader.utility;

import com.lichenaut.datapackloader.DatapackLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class DLUpdateChecker {

    private final JavaPlugin plugin;
    private final DatapackLoader dlPlugin;

    public DLUpdateChecker(JavaPlugin plugin, DatapackLoader dlPlugin) {this.plugin = plugin;this.dlPlugin = dlPlugin;}

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + 107149).openStream(); Scanner scanner = new
                    Scanner(inputStream)) {if (scanner.hasNext()) {consumer.accept(scanner.next());}
            } catch (IOException e) {
                dlPlugin.getLog().warning("Unable to check for updates!");
                e.printStackTrace();
            }
        });
    }
}