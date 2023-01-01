package com.lichenaut.datapackloader;

import com.lichenaut.datapackloader.commands.DLCommand;
import com.lichenaut.datapackloader.commands.DLTabCompleter;
import com.lichenaut.datapackloader.urlimport.DLActiveDatapacksTracker;
import com.lichenaut.datapackloader.urlimport.DLImportChecker;
import com.lichenaut.datapackloader.urlimport.DLURLImporter;
import com.lichenaut.datapackloader.utility.*;
import com.lichenaut.datapackloader.utility.DLWorldsDeleter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public final class DatapackLoader extends JavaPlugin {

    private DatapackLoader plugin;
    private Logger log;
    private TreeMap<String, String> activeDatapacks;//keep track of url-imported datapacks' parent .zip names to prevent unnecessary url imports
    private String dataFolderPath;
    private String datapacksFolderPath;

    @Override
    public void onEnable() {
        plugin = this;
        log = getLogger();
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Configuration config = getConfig();

        if (config.getBoolean("disable-plugin")) {
            log.info("[DatapackLoader] Plugin disabled in config.yml.");
        } else {
            Properties properties = new Properties();
            try {
                properties.load(Files.newInputStream(Paths.get("server.properties")));
            } catch (IOException e) {
                log.warning(ChatColor.RED + "[DatapackLoader] IOException: Could not read from '" + ChatColor.RESET + "server.properties" + ChatColor.RED + "'!");
                e.printStackTrace();
            }
            String levelName = properties.getProperty("level-name");

            if (config.getBoolean("developer-mode")) {
                try {
                    new DLWorldsDeleter(plugin).deleteOldWorlds(levelName);
                } catch (IOException e) {
                    log.warning(ChatColor.RED + "[DatapackLoader] IOException: Could not delete worlds!");
                    e.printStackTrace();
                }
            }

            dataFolderPath = getServer().getPluginsFolder() + DLFileSeparatorGetter.getSeparator() + "DatapackLoader";
            datapacksFolderPath = dataFolderPath + DLFileSeparatorGetter.getSeparator() + "Datapacks";

            DLResourceCreator resourceCreator = new DLResourceCreator(plugin);
            resourceCreator.createResource("README.txt");
            resourceCreator.createResource("activeDatapacks.txt");

            DLDirectoryMaker dirMaker = new DLDirectoryMaker(plugin);
            dirMaker.makeDir(datapacksFolderPath);

            activeDatapacks = new TreeMap<>();
            DLActiveDatapacksTracker activeDatapacksTracker = new DLActiveDatapacksTracker(plugin);
            activeDatapacksTracker.deserializePackList();
            activeDatapacksTracker.updatePackList();

            DLImportChecker importChecker = new DLImportChecker(plugin);
            DLURLImporter urlImporter = new DLURLImporter(plugin);
            boolean hasDatapacks = true;
            try {
                for (String stringUrl : config.getStringList("datapack-urls")) {
                    if (!stringUrl.endsWith(".zip")) {
                        log.warning(ChatColor.RED + "[DatapackLoader] URL '" + ChatColor.RESET + stringUrl + ChatColor.RED + "' must end with a .zip file! Skipping.");
                        continue;
                    }
                    URL url = new URL(stringUrl);
                    if (importChecker.checkUnnecessaryImport(url)) {urlImporter.importUrl(url);}
                }
                if (config.getBoolean("starter-datapack") && Objects.requireNonNull(new File(datapacksFolderPath).listFiles()).length == 0) {
                    URL url = new URL("https://github.com/misode/mcmeta/archive/refs/tags/" + getServer().getClass().getPackage().getName().split("\\.")[3] + "-data.zip");
                    if (importChecker.checkUnnecessaryImport(url)) {urlImporter.importUrl(url);}
                }
                if (Objects.requireNonNull(new File(getDatapacksFolderPath()).listFiles()).length == 0) {
                    log.warning(ChatColor.RED + "[DatapackLoader] The '" + ChatColor.RESET + "..." + datapacksFolderPath + ChatColor.RED + "' folder is empty!");
                    log.info(ChatColor.YELLOW + "[DatapackLoader] Read '" + ChatColor.RESET + "README.txt" + ChatColor.YELLOW +
                            "' for instructions. Thank you for trying DatapackLoader!");
                    hasDatapacks = false;
                }
            } catch (IOException e) {
                log.warning(ChatColor.RED + "[DatapackLoader] IOException: Could not import datapacks from the internet!");
                e.printStackTrace();
            } catch (NullPointerException e) {
                log.warning(ChatColor.RED + "[DatapackLoader] NullPointerException: Could not import datapacks from the internet!");
                e.printStackTrace();
            }

            activeDatapacksTracker.serializePackList();
            Objects.requireNonNull(getCommand("dl")).setExecutor(new DLCommand(plugin));
            Objects.requireNonNull(getCommand("dl")).setTabCompleter(new DLTabCompleter());

            if (hasDatapacks) {
                DLDatapackApplier datapackApplier = new DLDatapackApplier(plugin);
                boolean importEvent = datapackApplier.applyDatapacks(levelName);
                if (importEvent) {
                    getLog().info(ChatColor.GREEN + "[DatapackLoader] Restarting server to apply new datapacks!");
                    getServer().shutdown();
                }

                if (!importEvent) {
                    if (config.getBoolean("developer-mode")) {new DLLevelChanger(plugin).changeLevelName();}
                }
            }
        }
    }

    public DatapackLoader getPlugin() {return plugin;}
    public Logger getLog() {return log;}
    public TreeMap<String, String> getActiveDatapacks() {return activeDatapacks;}
    public String getPluginFolderPath() {return dataFolderPath;}
    public String getDatapacksFolderPath() {return datapacksFolderPath;}
    public void addToActiveDatapacks(String key, String value) {activeDatapacks.put(key, value);}
}