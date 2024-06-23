package com.lichenaut.datapackloader;

import com.lichenaut.datapackloader.cmd.*;
import com.lichenaut.datapackloader.dp.Applier;
import com.lichenaut.datapackloader.dp.Checker;
import com.lichenaut.datapackloader.dp.Finder;
import com.lichenaut.datapackloader.dp.Importer;
import com.lichenaut.datapackloader.util.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public final class Main extends JavaPlugin {

    private static final Logger logger = LogManager.getLogger("DatapackLoader");
    private static final String separator = FileSystems.getDefault().getSeparator();
    private final Messager messager = new Messager(this, logger);
    private CompletableFuture<Void> mainFuture = CompletableFuture.completedFuture(null);
    private PluginCommand dlCommand;
    private PluginCommand dltpCommand;

    @Override
    public void onEnable() {
        new Metrics(this, 17272);
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Configuration config = getConfig();
        if (config.getBoolean("disable-plugin")) {
            logger.info("Plugin disabled in config.yml.");
            return;
        }

        new VersionGetter(this).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                logger.info("Update available.");
            }
        });

        String datapacksFolderPath = getDataFolder().getPath() + separator + "datapacks";
        File datapacksFolder = new File(datapacksFolderPath);
        if (!datapacksFolder.mkdirs() && !datapacksFolder.exists()) {
            throw new RuntimeException("Failed to create 'datapacks' folder!");
        }

        Finder datapackFinder = new Finder(logger, this, separator);
        File[] files = datapacksFolder.listFiles();
        if (files != null) {
            for (File file : files) { // This is for datapack .zips added manually.
                String fileName = file.getName();
                if (fileName.endsWith(".zip")) {
                    try {
                        datapackFinder.fileWalk(datapacksFolderPath, file, true);
                    } catch (IOException e) {
                        throw new RuntimeException("IOException: Failed to walk .zip file!", e);
                    }
                } else {
                    if (Checker.isDatapack(file.getPath())) {
                        continue;
                    }

                    try {
                        datapackFinder.fileWalk(datapacksFolderPath, file, false);
                    } catch (IOException e) {
                        throw new RuntimeException("IOException: Failed to walk file!", e);
                    }
                }
            }
        }

        Importer importer = new Importer(logger, this, separator);
        mainFuture = mainFuture
                .thenAcceptAsync(declared -> {
                    File[] datapacksFolderFiles = new File(datapacksFolderPath).listFiles();
                    if (datapacksFolderFiles == null || datapacksFolderFiles.length != 0) {
                        return;
                    }

                    if (config.getBoolean("starter-datapack")) {
                        try {
                            importer.importUrl(datapacksFolderPath,
                                    new URI("https://github.com/misode/mcmeta/archive/refs/tags/"
                                            + getServer().getVersion().split("MC: ")[1].split("[)]")[0] + "-data.zip")
                                            .toURL());
                        } catch (IOException e) {
                            throw new RuntimeException("IOException: Failed to import starter datapack!", e);
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(
                                    "URISyntaxException: Failed to convert starter datapack string to URL!", e);
                        }
                    } else {
                        logger.info("The '...{}' folder is empty. Please execute command 'dl help'.",
                                datapacksFolderPath);
                    }
                });

        mainFuture = mainFuture
                .thenAcceptAsync(imported -> {
                    Properties properties = new Properties();
                    try {
                        properties.load(Files.newInputStream(Paths.get("server.properties")));
                    } catch (IOException e) {
                        throw new RuntimeException("IOException: Failed to load 'server.properties'!", e);
                    }

                    boolean importEvent;
                    String levelName = properties.getProperty("level-name");
                    String worldDatapacksPath = getServer().getWorldContainer() + separator + levelName + separator
                            + "datapacks";
                    Applier datapackApplier = new Applier(separator);
                    try {
                        importEvent = datapackApplier.applyDatapacks(datapacksFolder, worldDatapacksPath);
                    } catch (IOException e) {
                        throw new RuntimeException("IOException: Failed to apply datapacks!", e);
                    }

                    if (config.getBoolean("developer-mode") && !config.getBoolean("dev-mode-applied")) {
                        try {
                            new WorldsDeleter().deleteOldWorlds(
                                    Objects.requireNonNull(this.getServer().getWorldContainer().listFiles()),
                                    levelName);
                        } catch (IOException e) {
                            throw new RuntimeException("IOException: Failed to delete old worlds!", e);
                        }

                        try {
                            new LevelChanger(logger).changeLevelName();
                        } catch (IOException e) {
                            throw new RuntimeException(
                                    "IOException: Failed to change 'level-name' in 'server.properties'!", e);
                        }

                        config.set("dev-mode-applied", true);
                    } else {
                        config.set("dev-mode-applied", false);
                    }

                    saveConfig();
                    if (importEvent) {
                        logger.info("Restarting server to apply new datapacks!");
                        getServer().shutdown();
                    }
                });

        dlCommand = getCommand("dl");
        dltpCommand = getCommand("dltp");
        mainFuture = mainFuture
                .thenAcceptAsync(applied -> {
                    Cmd cmd = new Cmd();
                    dlCommand.setExecutor(new DLCmd(cmd, datapacksFolderPath, logger, this, messager, separator));
                    dlCommand.setTabCompleter(new DLTab());
                    dltpCommand.setExecutor(new DLTPCmd(cmd, this, messager));
                    dltpCommand.setTabCompleter(new DLTPTab(this));
                });
    }
}