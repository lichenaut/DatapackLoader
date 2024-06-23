package com.lichenaut.datapackloader.dp;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public class Applier {

    private final String separator;

    public boolean applyDatapacks(File datapacksFolder, String worldDatapacksPath) throws IOException {
        File[] datapacksFolderList = datapacksFolder.listFiles();
        if (datapacksFolderList == null) {
            return false;
        }

        File worldDatapacks = new File(worldDatapacksPath);
        if (!worldDatapacks.mkdirs() && !worldDatapacks.exists()) {
            throw new RuntimeException("Could not create directory '" + worldDatapacksPath + "'!");
        }

        boolean importEvent = false;
        for (File datapack : datapacksFolderList) {
            if (!datapack.isDirectory()) {
                continue;
            }

            File datapackTarget = new File(worldDatapacksPath + separator + datapack.getName());
            if (datapackTarget.exists()) {
                continue;
            }

            try {
                FileUtils.copyDirectory(datapack, datapackTarget);
                importEvent = true;
            } catch (IOException e) {
                throw new IOException(
                        "IOException: Could not copy directory '" + datapack + "' to '" + datapackTarget + "'!\n", e);
            }
        }

        return importEvent;
    }
}