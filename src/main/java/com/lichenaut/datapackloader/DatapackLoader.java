package com.lichenaut.datapackloader;

import com.lichenaut.datapackloader.urlimport.DLURLImporter;
import com.lichenaut.datapackloader.utility.*;
import com.lichenaut.datapackloader.utility.DLWorldsDeleter;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class DatapackLoader extends JavaPlugin {

    public ConsoleCommandSender console;
    public Configuration config;
    public TreeMap<String, String> activeDatapacks;//keep track of url-imported datapacks' parent .zip names to prevent unnecessary url imports
    public String dataPackLoaderPath;//path to 'DatapackLoader' folder
    public String datapacksPath;//path to 'Datapacks' folder

    @Override
    public void onEnable() {
        //setup
        DatapackLoader plugin = this;
        console = getServer().getConsoleSender();
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        config = getConfig();
        //disable plugin if config setting 'disable-plugin' is true
        if (config.getBoolean("disable-plugin")) {
            console.sendMessage("[DatapackLoader] Plugin disabled in config.yml.");
        } else {
            //load properties file and get the value of level-name
            Properties properties = new Properties();
            try {
                properties.load(Files.newInputStream(Paths.get("server.properties")));
            } catch (IOException e) {
                console.sendMessage(ChatColor.RED + "[DatapackLoader] IOException: Could not read from '" + ChatColor.RESET + "server.properties" + ChatColor.RED + "'!");
                e.printStackTrace();
            }
            String levelName = properties.getProperty("level-name");
            //delete old worlds if developer mode is on
            if (config.getBoolean("developer-mode")) {
                try {
                    new DLWorldsDeleter(plugin).deleteOldWorlds(levelName);
                } catch (IOException e) {
                    console.sendMessage(ChatColor.RED + "[DatapackLoader] IOException: Could not delete worlds!");
                    e.printStackTrace();
                }
            }
            //string paths for plugin data folder and 'Datapacks' folder
            dataPackLoaderPath = getServer().getPluginsFolder() + DLFileSeparatorGetter.getSeparator() + "DatapackLoader";
            datapacksPath = dataPackLoaderPath + DLFileSeparatorGetter.getSeparator() + "Datapacks";
            File datapacksFolder = new File(datapacksPath);
            //create readme file
            String readMePath = dataPackLoaderPath + DLFileSeparatorGetter.getSeparator() + "README.txt";
            if (!new File(readMePath).exists()) {
                try {
                    DLCopier.byteCopy(Objects.requireNonNull(getResource("README.txt")), readMePath);
                } catch (IOException e) {
                    console.sendMessage(ChatColor.RED + "[DatapackLoader] IOException: Could not generate '" + ChatColor.RESET + "README.txt" + ChatColor.RED + "'!");
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    console.sendMessage(ChatColor.RED + "[DatapackLoader] NullPointerException: Could not generate '" + ChatColor.RESET + "README.txt" + ChatColor.RED + "'!");
                    e.printStackTrace();
                }
            }
            //create file to hold serialized activeDatapacks data
            String activeDatapacksPath = dataPackLoaderPath + DLFileSeparatorGetter.getSeparator() + "activeDatapacks.txt";
            File activeDatapacksTextFile = new File(activeDatapacksPath);
            if (!activeDatapacksTextFile.exists()) {
                try {
                    DLCopier.byteCopy(Objects.requireNonNull(getResource("activeDatapacks.txt")), activeDatapacksPath);
                } catch (IOException e) {
                    console.sendMessage(ChatColor.RED + "[DatapackLoader] IOException: Could not generate '" + ChatColor.RESET + "activeDatapacks.txt" + ChatColor.RED + "'!");
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    console.sendMessage(ChatColor.RED + "[DatapackLoader] NullPointerException: Could not generate '" + ChatColor.RESET + "activeDatapacks.txt" + ChatColor.RED + "'!");
                    e.printStackTrace();
                }
            }
            //deserialize activeDatapacks map from file data
            activeDatapacks = new TreeMap<>();
            try (Scanner activeDatapacksScanner = new Scanner(activeDatapacksTextFile)) {
                while (activeDatapacksScanner.hasNextLine()) {
                    String[] keyValue = activeDatapacksScanner.nextLine().split(" ");
                    activeDatapacks.put(keyValue[0], keyValue[1]);
                }
            } catch (FileNotFoundException e) {
                console.sendMessage(ChatColor.RED + "[DatapackLoader] FileNotFoundException: Could not read from '" + ChatColor.RESET + "activeDatapacks.txt" + ChatColor.RED + "'!");
                e.printStackTrace();
            }
            //update activeDatapacks treemap so only necessary URL imports occur
            DLDirectoryMaker dirMaker = new DLDirectoryMaker(plugin);
            dirMaker.makeDir(datapacksPath);
            if (activeDatapacks != null) {
                //make list of file directory names in 'Datapacks' folder
                List<String> currentDatapacks = new ArrayList<>();
                for (File file : Objects.requireNonNull(datapacksFolder.listFiles())) {
                    if (file.isDirectory()) {
                        currentDatapacks.add(file.getName());
                    }
                }
                //remove datapacks in both the clone treemap and the list from both the clone treemap and the list
                TreeMap<String, String> activeDatapacksClone = new TreeMap<>();
                activeDatapacksClone.putAll(activeDatapacks);
                for (Map.Entry<String, String> entry : activeDatapacksClone.entrySet()) {
                    for (String name : currentDatapacks) {
                        if (entry.getKey().equals(name)) {
                            activeDatapacksClone.remove(name);
                            currentDatapacks.remove(entry.getKey());
                            break;
                        }
                    }
                }
                //remove all keys from treemap that share values with each of the remaining entries in the clone treemap, unless the value is "hand"
                //(rationale: if a .zip supplies multiple datapacks and only one of them was removed, re-download the .zip)
                for (Map.Entry<String, String> cloneEntry : activeDatapacksClone.entrySet()) {
                    if (!cloneEntry.getValue().equals("hand")) {
                        for (Map.Entry<String, String> entry : activeDatapacks.entrySet()) {
                            if (cloneEntry.getValue().equals(entry.getValue())) {
                                activeDatapacks.remove(entry.getKey());
                                break;
                            }
                        }
                    }
                }
                //add all remaining names in the list to the treemap with value "hand" (rationale: this adds to the map all datapacks added to the 'Datapacks' folder by hand)
                for (String name : currentDatapacks) {
                    activeDatapacks.put(name, "hand");
                }
            }
            //import from URLs in config, check if 'Datapacks' folder is empty
            DLURLImporter urlImporter = new DLURLImporter(plugin);
            DLImportChecker importChecker = new DLImportChecker(plugin);
            try {
                for (String stringUrl : config.getStringList("datapack-urls")) {
                    if (!stringUrl.endsWith(".zip")) {
                        console.sendMessage(ChatColor.RED + "[DatapackLoader] URL '" + ChatColor.RESET + stringUrl + ChatColor.RED +
                                "' must end with a .zip file! Skipping.");
                        continue;
                    }
                    URL url = new URL(stringUrl);
                    if (importChecker.checkUnnecessaryImport(url)) {
                        urlImporter.urlImport(url);
                    }
                }
                if (config.getBoolean("starter-datapack") && Objects.requireNonNull(datapacksFolder.listFiles()).length == 0) {
                    URL url = new URL("https://github.com/misode/mcmeta/archive/refs/tags/" + getServer().getClass().getPackage().getName().split("\\.")[3] + "-data.zip");
                    if (importChecker.checkUnnecessaryImport(url)) {
                        urlImporter.urlImport(url);
                    }
                }
                if (Objects.requireNonNull(datapacksFolder.listFiles()).length == 0) {
                    console.sendMessage(ChatColor.RED + "[DatapackLoader] The '" + ChatColor.RESET + "..." + datapacksPath + ChatColor.RED + "' folder is empty!");
                    console.sendMessage(ChatColor.YELLOW + "[DatapackLoader] Read '" + ChatColor.RESET + "README.txt" + ChatColor.YELLOW +
                            "' for instructions. Thank you for trying DatapackLoader!");
                    getServer().getPluginManager().disablePlugin(plugin);
                }
            } catch (IOException e) {
                console.sendMessage(ChatColor.RED + "[DatapackLoader] IOException: Could not generate plugin folders!");
                e.printStackTrace();
            } catch (NullPointerException e) {
                console.sendMessage(ChatColor.RED + "[DatapackLoader] NullPointerException: Could not generate plugin folders!");
                e.printStackTrace();
            }
            //serialize activeDatapacks map data to file
            if (activeDatapacks != null) {
                try (FileWriter fileWriter = new FileWriter(activeDatapacksTextFile)) {
                    for (Map.Entry<String, String> entry : activeDatapacks.entrySet()) {
                        fileWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
                    }
                } catch (IOException e) {
                    console.sendMessage(ChatColor.RED + "[DatapackLoader] IOException: Could not write to '" + ChatColor.RESET + activeDatapacksPath + ChatColor.RED + "'!");
                    e.printStackTrace();
                }
            }
            //move 'Datapacks' folder contents to world folder
            String worldDatapacksPath = getServer().getWorldContainer() + DLFileSeparatorGetter.getSeparator() + levelName + DLFileSeparatorGetter.getSeparator() + "datapacks";
            dirMaker.makeDir(worldDatapacksPath);
            boolean importEvent = false;
            for (File datapack : Objects.requireNonNull(datapacksFolder.listFiles())) {
                if (datapack.isDirectory()) {
                    File datapackTarget = new File(worldDatapacksPath + DLFileSeparatorGetter.getSeparator() + datapack.getName());
                    try {
                        if (datapackTarget.exists()) {
                            continue;
                        }
                        FileUtils.copyDirectory(datapack, datapackTarget);
                        importEvent = true;
                    } catch (IOException e) {
                        console.sendMessage(ChatColor.RED + "[DatapackLoader] IOException: Could not move datapack '" + ChatColor.RESET + datapack.getName() + ChatColor.RED +
                                "' to world folder!");
                        e.printStackTrace();
                    }
                }
            }
            if (importEvent) {
                getServer().shutdown();
            }
            //change level-name if developer mode is on
            if (config.getBoolean("developer-mode") && !importEvent) {
                console.sendMessage("[DatapackLoader] Altering 'level-name' in 'server.properties' because developer mode is on.");
                console.sendMessage("[DatapackLoader] This allows for new worlds to generate after server shutdown.");
                List<String> lines = new ArrayList<>();
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader("server.properties"))) {
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        lines.add(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                for (String string : lines) {
                    if (string.startsWith("level-name=")) {
                        if (string.equals("level-name=world")) {
                            lines.set(lines.indexOf(string), "level-name=wor1d");
                        } else {
                            lines.set(lines.indexOf(string), "level-name=world");
                        }
                        break;
                    }
                }
                try (FileWriter fileWriter = new FileWriter("server.properties")) {
                    for (String string : lines) {
                        fileWriter.write(string + "\n");
                    }
                } catch (IOException e) {
                    console.sendMessage(ChatColor.RED + "[DatapackLoader] IOException: Could not write to '" + ChatColor.RESET + activeDatapacksPath + ChatColor.RED + "'!");
                    e.printStackTrace();
                }
            }
            //register import and help commands
            //Objects.requireNonNull(getCommand("import")).setExecutor(new ImportCommand());
        }
    }
}