package com.lichenaut.datapackloader.util;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class VersionGetter {

    private final JavaPlugin plugin;
    private final GenUtil genUtil;

    public void getVersion(final Consumer<String> consumer) {
        if (genUtil.isFolia()) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, task -> fetchVersion(consumer));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> fetchVersion(consumer));
        }
    }

    private void fetchVersion(Consumer<String> consumer) {
        try (InputStream inputStream = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + 107149)
                .toURL()
                .openStream(); Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
                consumer.accept(scanner.next());
            }
        } catch (IOException e) {
            throw new RuntimeException("IOException: Unable to check for updates!", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URISyntaxException: Unable to check for updates!", e);
        }
    }
}