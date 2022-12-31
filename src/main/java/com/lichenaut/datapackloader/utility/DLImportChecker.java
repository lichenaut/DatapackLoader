package com.lichenaut.datapackloader.utility;

import com.lichenaut.datapackloader.DatapackLoader;
import org.apache.commons.io.FilenameUtils;

import java.net.URL;
import java.util.Map;

public class DLImportChecker {

    private final DatapackLoader plugin;

    public DLImportChecker(DatapackLoader plugin) {this.plugin = plugin;}

    public boolean checkUnnecessaryImport(URL url) {
        if (plugin.activeDatapacks != null) {
            for (Map.Entry<String, String> entry : plugin.activeDatapacks.entrySet()) {
                if (FilenameUtils.getName(url.getPath()).equals(entry.getValue())) {return false;}
            }
        }
        return true;
    }
}
