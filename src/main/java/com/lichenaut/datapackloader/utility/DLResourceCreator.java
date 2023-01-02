package com.lichenaut.datapackloader.utility;

import com.lichenaut.datapackloader.DatapackLoader;

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
                plugin.getLog().severe("IOException: Could not generate '" + resourceName + "'!");
                e.printStackTrace();
            } catch (NullPointerException e) {
                plugin.getLog().severe("NullPointerException: Could not generate '" + resourceName + "'!");
                e.printStackTrace();
            }
        }
    }
}