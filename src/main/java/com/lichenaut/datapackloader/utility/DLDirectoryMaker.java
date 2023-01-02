package com.lichenaut.datapackloader.utility;

import com.lichenaut.datapackloader.DatapackLoader;

import java.io.File;

public class DLDirectoryMaker {

    private final DatapackLoader plugin;

    public DLDirectoryMaker(DatapackLoader plugin) {this.plugin = plugin;}

    public void makeDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {if (!dir.mkdirs()) {plugin.getLog().severe("Could not create file '" + path + "'! SecurityException?");}}
    }
}