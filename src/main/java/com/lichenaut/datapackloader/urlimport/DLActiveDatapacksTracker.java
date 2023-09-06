package com.lichenaut.datapackloader.urlimport;

import com.lichenaut.datapackloader.DatapackLoader;
import com.lichenaut.datapackloader.utility.DLDatapackChecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DLActiveDatapacksTracker {

    private final DatapackLoader plugin;

    public DLActiveDatapacksTracker(DatapackLoader plugin) {this.plugin = plugin;}

    public void deserializePackList(String sourceListPath) throws FileNotFoundException {
        Scanner activeDatapacksScanner = new Scanner(new File(sourceListPath));
        while (activeDatapacksScanner.hasNextLine()) {
            String[] keyValue = activeDatapacksScanner.nextLine().split(" ");
            plugin.getActiveDatapacks().put(keyValue[0], keyValue[1]);
        }
    }

    public void updatePackList(String datapacksFolderPath) {
        File[] datapacksFolderContents = new File(datapacksFolderPath).listFiles();
        if (datapacksFolderContents == null) return;

        HashSet<String> currentDatapacks = new HashSet<>();// Make a list of the file directory names in 'datapacks' folder.
        for (File file : datapacksFolderContents) if (file.isDirectory() && DLDatapackChecker.isDatapack(file.getPath())) currentDatapacks.add(file.getName());

        HashMap<String, String> activeDatapacksClone = new HashMap<>(plugin.getActiveDatapacks());
        for (Iterator<String> iterator = currentDatapacks.iterator(); iterator.hasNext();) {
            String iteration = iterator.next();
            if (activeDatapacksClone.containsKey(iteration)) {// Remove datapacks in both the clone treemap and the list from both the clone treemap and the list.
                iterator.remove();
                activeDatapacksClone.remove(iteration);
                break;
            }
        }

        // Remove all keys from activeDatapacks that share values with each of the remaining entries in the clone, unless the value is "hand". If "hand", just remove the entry.
        // Rationale: if a .zip supplies multiple datapacks, and only one of them was removed, re-download the entire .zip.
        for (Map.Entry<String, String> cloneEntry : activeDatapacksClone.entrySet()) {
            String val = cloneEntry.getValue();
            if (val.equals("hand")) plugin.getActiveDatapacks().remove(cloneEntry.getKey()); else plugin.getActiveDatapacks().values().removeAll(Collections.singleton(val));
        }

        // Add all remaining datapack names in currentDatapacks with the value "hand" to activeDatapacks to represent that they were added by hand.
        for (String name : currentDatapacks) plugin.getActiveDatapacks().put(name, "hand");
    }

    public void serializePackList(String sourceListPath) {
        try (FileWriter fileWriter = new FileWriter(sourceListPath)) {
            for (Map.Entry<String, String> entry : plugin.getActiveDatapacks().entrySet()) fileWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
        } catch (IOException e) {e.printStackTrace();}
    }
}