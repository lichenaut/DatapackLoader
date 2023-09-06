package com.lichenaut.datapackloader;

import com.lichenaut.datapackloader.commands.DLCommand;
import com.lichenaut.datapackloader.commands.DLTPCommand;
import com.lichenaut.datapackloader.commands.DLTPTabCompleter;
import com.lichenaut.datapackloader.commands.DLTabCompleter;
import com.lichenaut.datapackloader.urlimport.DLActiveDatapacksTracker;
import com.lichenaut.datapackloader.urlimport.DLDatapackFinder;
import com.lichenaut.datapackloader.urlimport.DLURLImporter;
import com.lichenaut.datapackloader.utility.*;
import org.apache.commons.io.FilenameUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored", "unused"})
public final class DatapackLoader extends JavaPlugin {

    private Logger log;
    private HashMap<String, String> activeDatapacks;// Keeps track of url-imported datapacks' parent .zip names to prevent unnecessary url imports.

    @Override
    public void onEnable() {
        log = getLogger();
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Configuration config = getConfig();

        int pluginId = 17272;
        Metrics metrics = new Metrics(this, pluginId);

        if (config.getBoolean("disable-plugin")) {log.info("Plugin disabled in config.yml.");return;}

        new DLUpdateChecker(this, this).getVersion(version -> {if (!this.getDescription().getVersion().equals(version)) {getLog().warning("Update available.");}});

        Properties properties = new Properties();
        try {properties.load(Files.newInputStream(Paths.get("server.properties")));} catch (IOException e) {e.printStackTrace();}
        String levelName = properties.getProperty("level-name");

        if (config.getBoolean("developer-mode")) try {new DLWorldsDeleter(this).deleteOldWorlds(levelName);} catch (IOException e) {e.printStackTrace();}

        String dataFolderPath = getDataFolder().getPath();
        String datapacksFolderPath = dataFolderPath + DLSep.getSep() + "datapacks";
        File datapacksFolder = new File(datapacksFolderPath);
        if (!datapacksFolder.exists()) datapacksFolder.mkdirs();

        String resourcePath = dataFolderPath + DLSep.getSep() + "README.txt";
        if (!new File(resourcePath).exists()) {try {DLCopier.smallCopy(this.getResource("README.txt"), resourcePath);} catch (IOException e) {throw new RuntimeException(e);}}
        resourcePath = dataFolderPath + DLSep.getSep() + "sourceList.txt";
        if (!new File(resourcePath).exists()) {try {DLCopier.smallCopy(this.getResource("sourceList.txt"), resourcePath);} catch (IOException e) {throw new RuntimeException(e);}}

        activeDatapacks = new HashMap<>();
        DLActiveDatapacksTracker activeDatapacksTracker = new DLActiveDatapacksTracker(this);
        try {activeDatapacksTracker.deserializePackList(resourcePath);} catch (FileNotFoundException e) {throw new RuntimeException(e);}
        activeDatapacksTracker.updatePackList(datapacksFolderPath);

        DLURLImporter urlImporter = new DLURLImporter(this);
        boolean hasDatapack = true, importEvent = false;
        tryBlock:
        {
            try {manualScan:
                {
                    DLDatapackFinder datapackFinder = new DLDatapackFinder(this, "hand");
                    File[] files = new File(datapacksFolderPath).listFiles();
                    if (files == null) break manualScan;
                    for (File file : files) { // This scan is for datapack .zips added manually
                        if (file.getName().endsWith(".zip")) {
                            if (datapackFinder.fileWalk(datapacksFolderPath, file, true)) importEvent = true;
                        } else {
                            if (DLDatapackChecker.isDatapack(file.getPath())) continue;

                            if (datapackFinder.fileWalk(datapacksFolderPath, file, false)) {
                                activeDatapacks.remove(file.getName());
                                importEvent = true;
                            }
                        }
                    }
                }

                for (String stringUrl : config.getStringList("datapack-urls")) {
                    if (!stringUrl.endsWith(".zip")) {log.severe("URL '" + stringUrl + "' must end with a .zip file! Skipping.");continue;}

                    URL url = new URL(stringUrl);
                    if (!getActiveDatapacks().containsValue(FilenameUtils.getName(url.getPath()))) urlImporter.importUrl(datapacksFolderPath, url);
                }

                File[] datapacksFolderFiles = new File(datapacksFolderPath).listFiles();
                if (datapacksFolderFiles == null || datapacksFolderFiles.length != 0) break tryBlock;

                if (config.getBoolean("starter-datapack")) {
                    URL url = new URL("https://github.com/misode/mcmeta/archive/refs/tags/" + getServer().getVersion().split("MC: ")[1].split("[)]")[0] + "-data.zip");
                    urlImporter.importUrl(datapacksFolderPath, url);
                } else {
                    log.warning("The '..." + datapacksFolderPath + "' folder is empty! Please read 'README.txt' for instructions.");
                    hasDatapack = false;
                }
            } catch (IOException | NullPointerException e) {e.printStackTrace();}
        }

        activeDatapacksTracker.serializePackList(resourcePath);

        Objects.requireNonNull(getCommand("dl")).setExecutor(new DLCommand(this, datapacksFolderPath));
        Objects.requireNonNull(getCommand("dl")).setTabCompleter(new DLTabCompleter());
        Objects.requireNonNull(getCommand("dltp")).setExecutor(new DLTPCommand(this));
        Objects.requireNonNull(getCommand("dltp")).setTabCompleter(new DLTPTabCompleter(this));

        if (hasDatapack) {
            if (config.getBoolean("developer-mode")) {new DLLevelChanger(this).changeLevelName();}
            String worldDatapacksPath = getServer().getWorldContainer() + DLSep.getSep() + levelName + DLSep.getSep() + "datapacks";
            DLDatapackApplier datapackApplier = new DLDatapackApplier();
            if (importEvent) datapackApplier.applyDatapacks(datapacksFolder, worldDatapacksPath);
            else importEvent = datapackApplier.applyDatapacks(datapacksFolder, worldDatapacksPath);
            if (importEvent) {
                log.warning("Restarting server to apply new datapacks!");
                getServer().shutdown();
            }
        }
    }

    public Logger getLog() {return log;}
    public HashMap<String, String> getActiveDatapacks() {return activeDatapacks;}
}