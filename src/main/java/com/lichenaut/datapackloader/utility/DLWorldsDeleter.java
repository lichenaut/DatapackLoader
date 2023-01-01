package com.lichenaut.datapackloader.utility;

import com.lichenaut.datapackloader.DatapackLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DLWorldsDeleter {

    private final DatapackLoader plugin;

    public DLWorldsDeleter(DatapackLoader plugin) {this.plugin = plugin;}

    public void deleteOldWorlds(String levelName) throws IOException {
        //check for a 'level.dat', 'session.lock', and at least one folder to verify that it is a world
        for (File file : Objects.requireNonNull(plugin.getServer().getWorldContainer().listFiles())) {
            boolean levelDat = false;boolean sessionLock = false;boolean hasFolder = false;
            if (file.isDirectory() && !file.getName().startsWith(levelName)) {
                for (File f : Objects.requireNonNull(file.listFiles())) {
                    if (!f.isDirectory()) {
                        if (f.getName().equals("level.dat")) {levelDat = true;continue;}
                        if (f.getName().equals("session.lock")) {sessionLock = true;}
                        continue;
                    }
                    if (f.isDirectory() && !hasFolder) {hasFolder = true;}
                }
                if (levelDat && sessionLock && hasFolder) {FileUtils.deleteDirectory(file);}
            }
        }
    }
}