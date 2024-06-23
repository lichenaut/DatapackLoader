package com.lichenaut.datapackloader.dp;

import com.lichenaut.datapackloader.Main;
import com.lichenaut.datapackloader.util.Copier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@RequiredArgsConstructor
public class Importer {

    private final Logger logger;
    private final Main main;
    private final String separator;

    public void importUrl(String datapacksFolderPath, URL url) throws IOException, NullPointerException {
        String packZipPath = datapacksFolderPath + separator + FilenameUtils.getName(url.getPath());
        File packZip = new File(packZipPath);
        if (!packZip.exists()) {
            Copier.copy(new BufferedInputStream(url.openStream()), packZipPath);
        }
        new Finder(logger, main, separator).fileWalk(datapacksFolderPath, packZip, true);
    }
}