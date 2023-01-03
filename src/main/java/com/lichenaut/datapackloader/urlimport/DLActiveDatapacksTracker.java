package com.lichenaut.datapackloader.urlimport;

import com.lichenaut.datapackloader.DatapackLoader;
import com.lichenaut.datapackloader.utility.DLFileSeparatorGetter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DLActiveDatapacksTracker {

    private final DatapackLoader plugin;

    public DLActiveDatapacksTracker(DatapackLoader plugin) {this.plugin = plugin;}

    public void deserializePackList() {
        try (Scanner activeDatapacksScanner = new Scanner(new File(plugin.getPluginFolderPath() + DLFileSeparatorGetter.getSeparator() + "sourceList.txt"))) {
            while (activeDatapacksScanner.hasNextLine()) {
                String[] keyValue = activeDatapacksScanner.nextLine().split(" ");
                plugin.addToActiveDatapacks(keyValue[0], keyValue[1]);
            }
        } catch (FileNotFoundException e) {
            plugin.getLog().severe("FileNotFoundException: Could not read from 'sourceList.txt'!");
            e.printStackTrace();
        }
    }

    public void updatePackList() {
        //make list of file directory names in 'Datapacks' folder
        List<String> currentDatapacks = new ArrayList<>();
        for (File file : Objects.requireNonNull(new File(plugin.getDatapacksFolderPath()).listFiles())) {
            if (file.isDirectory()) {currentDatapacks.add(file.getName());}
        }
        //remove datapacks in both the clone treemap and the list from both the clone treemap and the list
        TreeMap<String, String> activeDatapacksClone = new TreeMap<>();
        activeDatapacksClone.putAll(plugin.getActiveDatapacks());
        for (Iterator<Map.Entry<String, String>> iterator = activeDatapacksClone.entrySet().iterator(); iterator.hasNext();) {
            String iterationKey = iterator.next().getKey();
            for (String name : currentDatapacks) {
                if (iterationKey.equals(name)) {
                    iterator.remove();
                    currentDatapacks.remove(iterationKey);
                    break;
                }
            }
        }
        //remove all keys from treemap that share values with each of the remaining entries in the clone treemap, unless the value is "hand". if it is, just remove that entry
        //rationale: if a .zip supplies multiple datapacks and only one of them was removed, re-download the entire .zip
        for (Map.Entry<String, String> cloneEntry : activeDatapacksClone.entrySet()) {
            if (cloneEntry.getValue().equals("hand")) {plugin.getActiveDatapacks().remove(cloneEntry.getKey());
            } else {
                for (Map.Entry<String, String> entry : plugin.getActiveDatapacks().entrySet()) {
                    if (cloneEntry.getValue().equals(entry.getValue())) {plugin.getActiveDatapacks().remove(entry.getKey());}
                }
            }
        }
        //add all remaining names in the list to the treemap with value "hand"
        //rationale: this adds to the map all datapacks added to the 'Datapacks' folder by hand
        for (String name : currentDatapacks) {plugin.addToActiveDatapacks(name, "hand");}
    }

    public void serializePackList() {
        try (FileWriter fileWriter = new FileWriter(plugin.getPluginFolderPath() + DLFileSeparatorGetter.getSeparator() + "sourceList.txt")) {
            for (Map.Entry<String, String> entry : plugin.getActiveDatapacks().entrySet()) {fileWriter.write(entry.getKey() + " " + entry.getValue() + "\n");}
        } catch (IOException e) {
            plugin.getLog().severe("IOException: Could not write to '" + plugin.getPluginFolderPath() + DLFileSeparatorGetter.getSeparator() + "sourceList.txt'!");
            e.printStackTrace();
        }
    }
}