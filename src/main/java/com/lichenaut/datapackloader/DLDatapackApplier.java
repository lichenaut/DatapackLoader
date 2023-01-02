package com.lichenaut.datapackloader;

import com.lichenaut.datapackloader.utility.DLDirectoryMaker;
import com.lichenaut.datapackloader.utility.DLFileSeparatorGetter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DLDatapackApplier {

    private final DatapackLoader plugin;

    public DLDatapackApplier(DatapackLoader plugin) {this.plugin = plugin;}

    public boolean applyDatapacks(String levelName) {
        String worldDatapacksPath = plugin.getServer().getWorldContainer() + DLFileSeparatorGetter.getSeparator() + levelName + DLFileSeparatorGetter.getSeparator() + "datapacks";
        new DLDirectoryMaker(plugin).makeDir(worldDatapacksPath);
        boolean importEvent = false;
        for (File datapack : Objects.requireNonNull(new File(plugin.getDatapacksFolderPath()).listFiles())) {
            if (datapack.isDirectory()) {
                File datapackTarget = new File(worldDatapacksPath + DLFileSeparatorGetter.getSeparator() + datapack.getName());
                try {
                    if (datapackTarget.exists()) {continue;}
                    FileUtils.copyDirectory(datapack, datapackTarget);
                    importEvent = true;
                } catch (IOException e) {
                    plugin.getLog().severe("IOException: Could not move datapack '" + datapack.getName() + "' to world folder!");
                    e.printStackTrace();
                }
            }
        }
        return importEvent;
    }
}