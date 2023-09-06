package com.lichenaut.datapackloader;

import com.lichenaut.datapackloader.utility.DLSep;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DLDatapackApplier {

    public boolean applyDatapacks(File datapacksFolder, String worldDatapacksPath) {
        File[] datapacksFolderList = datapacksFolder.listFiles();
        if (datapacksFolderList == null) return false;

        File worldDatapacks = new File(worldDatapacksPath);
        if (!worldDatapacks.exists()) worldDatapacks.mkdirs();

        boolean importEvent = false;
        for (File datapack : datapacksFolderList) {
            if (!datapack.isDirectory()) continue;

            File datapackTarget = new File(worldDatapacksPath + DLSep.getSep() + datapack.getName());
            if (datapackTarget.exists()) continue;

            try {FileUtils.copyDirectory(datapack, datapackTarget);
                importEvent = true;
            } catch (IOException e) {e.printStackTrace();}
        }

        return importEvent;
    }
}