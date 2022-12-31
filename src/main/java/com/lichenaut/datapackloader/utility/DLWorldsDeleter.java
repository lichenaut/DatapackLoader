package com.lichenaut.datapackloader.utility;

import com.lichenaut.datapackloader.DatapackLoader;
import org.apache.commons.io.FileUtils;
import org.bukkit.World;

import java.io.IOException;

public class DLWorldsDeleter {

    private final DatapackLoader plugin;

    public DLWorldsDeleter(DatapackLoader plugin) {this.plugin = plugin;}

    public void deleteWorlds(String levelName) throws IOException {
        for (World w : plugin.getServer().getWorlds()) {if (w != null && !w.getName().startsWith(levelName)) {FileUtils.deleteDirectory(w.getWorldFolder());}}
    }
}