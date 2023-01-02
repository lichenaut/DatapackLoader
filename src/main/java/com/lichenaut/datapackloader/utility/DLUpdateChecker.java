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
    private final int resourceId;

    public DLUpdateChecker(JavaPlugin plugin, DatapackLoader dlPlugin, int resourceId) {this.plugin = plugin;this.dlPlugin = dlPlugin;this.resourceId = resourceId;}

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {consumer.accept(scanner.next());}
            } catch (IOException e) {
                dlPlugin.getLog().warning("Unable to check for updates!");
                e.printStackTrace();
            }
        });
    }
}