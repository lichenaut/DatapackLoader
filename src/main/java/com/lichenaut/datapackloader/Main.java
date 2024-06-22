package com.lichenaut.datapackloader;

import com.lichenaut.datapackloader.cmd.DLCommand;
import com.lichenaut.datapackloader.cmd.DLTPCommand;
import com.lichenaut.datapackloader.cmd.DLTPTabCompleter;
import com.lichenaut.datapackloader.cmd.DLTabCompleter;
import com.lichenaut.datapackloader.url.ActiveDPsTracker;
import com.lichenaut.datapackloader.url.DPFinder;
import com.lichenaut.datapackloader.url.URLImporter;
import com.lichenaut.datapackloader.util.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public final class Main extends JavaPlugin {

    private static final Logger logger = LogManager.getLogger("DatapackLoader");
    private static final String separator = FileSystems.getDefault().getSeparator();
    private final Messager messager = new Messager(this, logger);
    // Keeps track of URL-imported datapacks' parent .zip names to prevent re-importing.
    private HashMap<String, String> activeDatapacks = new HashMap<>();
    private CompletableFuture<Void> mainFuture = CompletableFuture.completedFuture(null);

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

        new VersionGetter(logger, this).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                logger.info("Update available.");
            }
        });

        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(Paths.get("server.properties")));
        } catch (IOException e) {
            logger.error("IOException: Unable to load server.properties!\n{}", e.getMessage());
        }
        String levelName = properties.getProperty("level-name");
        if (config.getBoolean("developer-mode")) {
            try {
                new WorldsDeleter().deleteOldWorlds(Objects.requireNonNull(this.getServer().getWorldContainer().listFiles()), levelName);
            } catch (IOException e) {
                logger.error("IOException: Unable to delete old worlds!\n{}", e.getMessage());
            }
        }

        String dataFolderPath = getDataFolder().getPath();
        String datapacksFolderPath = dataFolderPath + separator + "datapacks";
        File datapacksFolder = new File(datapacksFolderPath);
        if (!datapacksFolder.mkdirs() && !datapacksFolder.exists()) {
            throw new RuntimeException("Failed to create 'datapacks' folder!");
        }

        String resourcePath = dataFolderPath + separator + "sourceList.txt";
        if (!new File(resourcePath).exists()) {
            try {
                Copier.smallCopy(this.getResource("sourceList.txt"), resourcePath);
            } catch (IOException e) {
                logger.error("IOException: Unable to copy sourceList.txt!\n{}", e.getMessage();
            }
        }

        ActiveDPsTracker activeDatapacksTracker = new ActiveDPsTracker(logger,this);
        try {
            activeDatapacksTracker.deserializePackList(resourcePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        activeDatapacksTracker.updatePackList(datapacksFolderPath);
        // TODO: stopped here
        URLImporter urlImporter = new URLImporter(logger, this, separator);
        boolean hasDatapack = true, importEvent = false;
        tryBlock: {
            try {
                manualScan: {
                    DPFinder datapackFinder = new DPFinder(this, "hand");
                    File[] files = new File(datapacksFolderPath).listFiles();
                    if (files == null)
                        break manualScan;
                    for (File file : files) { // This scan is for datapack .zips added manually
                        if (file.getName().endsWith(".zip")) {
                            if (datapackFinder.fileWalk(datapacksFolderPath, file, true))
                                importEvent = true;
                        } else {
                            if (DPChecker.isDatapack(file.getPath()))
                                continue;

                            if (datapackFinder.fileWalk(datapacksFolderPath, file, false)) {
                                activeDatapacks.remove(file.getName());
                                importEvent = true;
                            }
                        }
                    }
                }

                for (String stringUrl : config.getStringList("datapack-urls")) {
                    if (!stringUrl.endsWith(".zip")) {
                        log.severe("URL '" + stringUrl + "' must end with a .zip file! Skipping.");
                        continue;
                    }

                    URL url = new URL(stringUrl);
                    if (!getActiveDatapacks().containsValue(FilenameUtils.getName(url.getPath())))
                        urlImporter.importUrl(datapacksFolderPath, url);
                }

                File[] datapacksFolderFiles = new File(datapacksFolderPath).listFiles();
                if (datapacksFolderFiles == null || datapacksFolderFiles.length != 0)
                    break tryBlock;

                if (config.getBoolean("starter-datapack")) {
                    URL url = new URL("https://github.com/misode/mcmeta/archive/refs/tags/"
                            + getServer().getVersion().split("MC: ")[1].split("[)]")[0] + "-data.zip");
                    urlImporter.importUrl(datapacksFolderPath, url);
                } else {
                    log.warning("The '..." + datapacksFolderPath
                            + "' folder is empty! Please read 'README.txt' for instructions.");
                    hasDatapack = false;
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        activeDatapacksTracker.serializePackList(resourcePath);

        Objects.requireNonNull(getCommand("dl")).setExecutor(new DLCommand(this, datapacksFolderPath));
        Objects.requireNonNull(getCommand("dl")).setTabCompleter(new DLTabCompleter());
        Objects.requireNonNull(getCommand("dltp")).setExecutor(new DLTPCommand(this));
        Objects.requireNonNull(getCommand("dltp")).setTabCompleter(new DLTPTabCompleter(this));

        if (hasDatapack) {
            if (config.getBoolean("developer-mode")) {
                new LevelChanger(this).changeLevelName();
            }
            String worldDatapacksPath = getServer().getWorldContainer() + DLSep.getSep() + levelName + DLSep.getSep()
                    + "datapacks";
            DLDatapackApplier datapackApplier = new DLDatapackApplier();
            if (importEvent)
                datapackApplier.applyDatapacks(datapacksFolder, worldDatapacksPath);
            else
                importEvent = datapackApplier.applyDatapacks(datapacksFolder, worldDatapacksPath);
            if (importEvent) {
                log.warning("Restarting server to apply new datapacks!");
                getServer().shutdown();
            }
        }
    }
}