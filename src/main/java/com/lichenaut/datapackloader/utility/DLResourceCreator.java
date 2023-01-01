package com.lichenaut.datapackloader.utility;

import com.lichenaut.datapackloader.DatapackLoader;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class DLResourceCreator {

    private final DatapackLoader plugin;

    public DLResourceCreator(DatapackLoader plugin) {this.plugin = plugin;}

    public void createResource(String resourceName) {
        String resourcePath = plugin.getPluginFolderPath() + DLFileSeparatorGetter.getSeparator() + resourceName;
        if (!new File(resourcePath).exists()) {
            try {
                DLCopier.byteCopy(Objects.requireNonNull(plugin.getResource(resourceName)), resourcePath);
            } catch (IOException e) {
                plugin.getLog().warning(ChatColor.RED + "[DatapackLoader] IOException: Could not generate '" + ChatColor.RESET + resourceName + ChatColor.RED + "'!");
                e.printStackTrace();
            } catch (NullPointerException e) {
                plugin.getLog().warning(ChatColor.RED + "[DatapackLoader] NullPointerException: Could not generate '" + ChatColor.RESET + resourceName + ChatColor.RED + "'!");
                e.printStackTrace();
            }
        }
    }
}