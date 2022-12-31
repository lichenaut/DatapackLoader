package com.lichenaut.datapackloader.utility;

import com.lichenaut.datapackloader.DatapackLoader;
import org.bukkit.ChatColor;

import java.io.File;

public class DLDirectoryMaker {

    private final DatapackLoader plugin;

    public DLDirectoryMaker(DatapackLoader plugin) {this.plugin = plugin;}

    public void makeDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {if (!dir.mkdirs()) {plugin.console.sendMessage(ChatColor.RED + "[DatapackLoader] Could not create file '" + ChatColor.RESET + path + ChatColor.RED + "'! SecurityException?");}}
    }
}