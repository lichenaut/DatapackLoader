package com.lichenaut.datapackloader.url;

import com.lichenaut.datapackloader.Main;
import com.lichenaut.datapackloader.util.DPChecker;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
public class ActiveDPsTracker {

    private final Logger logger;
    private final Main main;

    public void deserializePackList(String sourceListPath) throws FileNotFoundException {
        Scanner activeDatapacksScanner = new Scanner(new File(sourceListPath));
        HashMap<String, String> activeDatapacks = new HashMap<>();
        while (activeDatapacksScanner.hasNextLine()) {
            String[] keyValue = activeDatapacksScanner.nextLine().split(" ");
            activeDatapacks.put(keyValue[0], keyValue[1]);
        }
        main.setActiveDatapacks(activeDatapacks);
    }

    public void updatePackList(String datapacksFolderPath) {
        File[] datapacksFolderContents = new File(datapacksFolderPath).listFiles();
        if (datapacksFolderContents == null) {
            return;
        }

        HashSet<String> currentDatapacks = new HashSet<>(); // Make a list of the file directory names in 'datapacks' folder.
        for (File file : datapacksFolderContents) {
            if (file.isDirectory() && DPChecker.isDatapack(file.getPath())) {
                currentDatapacks.add(file.getName());
            }
        }

        HashMap<String, String> activeDatapacks = main.getActiveDatapacks();
        HashMap<String, String> activeDatapacksClone = new HashMap<>(activeDatapacks);
        for (Iterator<String> iterator = currentDatapacks.iterator(); iterator.hasNext();) {
            String iteration = iterator.next();
            if (activeDatapacksClone.containsKey(iteration)) { // Remove datapacks in both the clone treemap and the list from both the clone treemap and the list.
                iterator.remove();
                activeDatapacksClone.remove(iteration);
                break;
            }
        }

        // Remove all keys from activeDatapacks that share values with each of the
        // remaining entries in the clone, unless the value is "hand". If "hand", just
        // remove the entry.
        // Rationale: if a .zip supplies multiple datapacks, and only one of them was
        // removed, re-download the entire .zip.
        for (Map.Entry<String, String> cloneEntry : activeDatapacksClone.entrySet()) {
            String val = cloneEntry.getValue();
            if (val.equals("hand")) {
                activeDatapacks.remove(cloneEntry.getKey());
            } else {
                activeDatapacks.values().removeAll(Collections.singleton(val));
            }
        }

        // Add all remaining datapack names in currentDatapacks with the value "hand" to
        // activeDatapacks to represent that they were added by hand.
        for (String name : currentDatapacks) {
            activeDatapacks.put(name, "hand");
        }

        main.setActiveDatapacks(activeDatapacks);
    }

    public void serializePackList(String sourceListPath) {
        try (FileWriter fileWriter = new FileWriter(sourceListPath)) {
            for (Map.Entry<String, String> entry : main.getActiveDatapacks().entrySet()) {
                fileWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}