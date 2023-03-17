package com.lichenaut.datapackloader.urlimport;

import com.lichenaut.datapackloader.DatapackLoader;
import org.apache.commons.io.FilenameUtils;

import java.net.URL;

public class DLImportChecker {

    private final DatapackLoader plugin;

    public DLImportChecker(DatapackLoader plugin) {this.plugin = plugin;}

    public boolean checkUnnecessaryImport(URL url) {return !plugin.getActiveDatapacks().containsValue(FilenameUtils.getName(url.getPath()));}
}
