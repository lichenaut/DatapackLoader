package com.lichenaut.datapackloader;

import com.lichenaut.datapackloader.commands.DLCommand;
import com.lichenaut.datapackloader.commands.DLTPCommand;
import com.lichenaut.datapackloader.commands.DLTPTabCompleter;
import com.lichenaut.datapackloader.commands.DLTabCompleter;
import com.lichenaut.datapackloader.urlimport.DLActiveDatapacksTracker;
import com.lichenaut.datapackloader.urlimport.DLImportChecker;
import com.lichenaut.datapackloader.urlimport.DLURLImporter;
import com.lichenaut.datapackloader.utility.*;
import com.lichenaut.datapackloader.utility.DLWorldsDeleter;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
public final class DatapackLoader extends JavaPlugin {

    private Logger log;
    private TreeMap<String, String> activeDatapacks;//keep track of url-imported datapacks' parent .zip names to prevent unnecessary url imports
    private String dataFolderPath;
    private String datapacksFolderPath;

    @Override
    public void onEnable() {
        final DatapackLoader plugin = this;
        log = getLogger();
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Configuration config = getConfig();

        if (config.getBoolean("disable-plugin")) {
            log.info("Plugin disabled in config.yml.");
        } else {
            Properties properties = new Properties();
            try {
                properties.load(Files.newInputStream(Paths.get("server.properties")));
            } catch (IOException e) {
                log.severe("IOException: Could not read from 'server.properties'!");
                e.printStackTrace();
            }
            String levelName = properties.getProperty("level-name");

            if (config.getBoolean("developer-mode")) {
                try {
                    new DLWorldsDeleter(plugin).deleteOldWorlds(levelName);
                } catch (IOException e) {
                    log.severe("IOException: Could not delete worlds!");
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
                        log.warning("URL '" + stringUrl + "' must end with a .zip file! Skipping.");
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
                    log.warning("The '..." + datapacksFolderPath + "' folder is empty!");
                    log.info("Read 'README.txt' for instructions. Thank you for trying DatapackLoader!");
                    hasDatapacks = false;
                }
            } catch (IOException e) {
                log.severe("IOException: Could not import datapacks from the internet!");
                e.printStackTrace();
            } catch (NullPointerException e) {
                log.severe("NullPointerException: Could not import datapacks from the internet!");
                e.printStackTrace();
            }

            activeDatapacksTracker.serializePackList();
            Objects.requireNonNull(getCommand("dl")).setExecutor(new DLCommand(plugin));
            Objects.requireNonNull(getCommand("dl")).setTabCompleter(new DLTabCompleter());
            Objects.requireNonNull(getCommand("dltp")).setExecutor(new DLTPCommand(plugin));
            Objects.requireNonNull(getCommand("dltp")).setTabCompleter(new DLTPTabCompleter(plugin));

            if (hasDatapacks) {
                DLDatapackApplier datapackApplier = new DLDatapackApplier(plugin);
                boolean importEvent = datapackApplier.applyDatapacks(levelName);
                if (importEvent) {
                    log.info("Restarting server to apply new datapacks!");
                    getServer().shutdown();
                }

                if (!importEvent) {if (config.getBoolean("developer-mode")) {new DLLevelChanger(plugin).changeLevelName();}}//write new code to inside this
            }
        }
    }

    public Logger getLog() {return log;}
    public TreeMap<String, String> getActiveDatapacks() {return activeDatapacks;}
    public String getPluginFolderPath() {return dataFolderPath;}
    public String getDatapacksFolderPath() {return datapacksFolderPath;}
    public void addToActiveDatapacks(String key, String value) {activeDatapacks.put(key, value);}
}