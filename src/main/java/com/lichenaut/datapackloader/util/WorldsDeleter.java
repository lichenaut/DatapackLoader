package com.lichenaut.datapackloader.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class WorldsDeleter {

    // private final File[] containerFiles; //
    // main.getServer().getWorldContainer().listFiles();

    public void deleteOldWorlds(File[] containerFiles, String levelName) throws IOException {
        for (File containerFile : containerFiles) {
            if (!containerFile.isDirectory() || containerFile.getName().startsWith(levelName)) { // Yes, continuing when
                                                                                                 // the name starts with
                                                                                                 // the level name is
                                                                                                 // intentional.
                                                                                                 // Developer mode
                                                                                                 // switched the level
                                                                                                 // name last server
                                                                                                 // instance.
                continue;
            }

            File[] files = containerFile.listFiles();
            if (files == null) {
                continue;
            }

            boolean hasFolder = false, levelDat = false, sessionLock = false;
            for (File file : files) {
                if (!hasFolder && file.isDirectory()) {
                    hasFolder = true;
                    continue;
                }

                String fileName = file.getName();
                if (!levelDat && fileName.equals("level.dat")) {
                    levelDat = true;
                } else if (!sessionLock && fileName.equals("session.lock")) {
                    sessionLock = true;
                }

                if (sessionLock && levelDat && hasFolder) {
                    FileUtils.deleteDirectory(containerFile);
                    break;
                }
            }
        }
    }
}