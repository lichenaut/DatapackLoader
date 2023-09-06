package com.lichenaut.datapackloader.utility;

import com.lichenaut.datapackloader.DatapackLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DLWorldsDeleter {

    private final File[] containerFiles;

    public DLWorldsDeleter(DatapackLoader plugin) {
        containerFiles = plugin.getServer().getWorldContainer().listFiles();
    }

    public void deleteOldWorlds(String levelName) throws IOException {
        for (File file : containerFiles) {
            if (!file.isDirectory() || file.getName().startsWith(levelName)) continue;

            File[] files = file.listFiles();
            if (files == null) continue;

            boolean hasFolder = false, levelDat = false, sessionLock = false;
            for (File f : files)
                if (f.isDirectory()) {if (!hasFolder) hasFolder = true;} else if (f.getName().equals("level.dat")) levelDat = true; else if (f.getName().equals("session.lock")) sessionLock = true;

            if (levelDat && sessionLock && hasFolder) {FileUtils.deleteDirectory(file);}
        }
    }
}