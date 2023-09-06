package com.lichenaut.datapackloader.urlimport;

import com.lichenaut.datapackloader.DatapackLoader;
import com.lichenaut.datapackloader.utility.DLCopier;
import com.lichenaut.datapackloader.utility.DLSep;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DLURLImporter {

    private final DatapackLoader plugin;

    public DLURLImporter(DatapackLoader plugin) {this.plugin = plugin;}

    public void importUrl(String datapacksFolderPath, URL url) throws IOException, NullPointerException {
        String packZipPath = datapacksFolderPath + DLSep.getSep() + FilenameUtils.getName(url.getPath());
        File packZip = new File(packZipPath);
        if (!packZip.exists()) DLCopier.copy(new BufferedInputStream(url.openStream()), packZipPath);
        new DLDatapackFinder(plugin, FilenameUtils.getName(url.getPath())).fileWalk(datapacksFolderPath, packZip, true);
    }
}