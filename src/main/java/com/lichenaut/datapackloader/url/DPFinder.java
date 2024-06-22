package com.lichenaut.datapackloader.url;

import com.lichenaut.datapackloader.Main;
import com.lichenaut.datapackloader.util.Copier;
import com.lichenaut.datapackloader.util.DPChecker;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.core.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RequiredArgsConstructor
public class DPFinder extends SimpleFileVisitor<Path> {

    private final Logger logger;
    private final Main main;
    private final String rootName;
    private final String separator;
    private boolean importEvent;

    public boolean fileWalk(String datapacksFolderPath, File file, boolean isZip) throws IOException {
        String targetFilePath = isZip ? datapacksFolderPath + separator
                + file.getName().substring(0, file.getName().length() - 4)
                : file.getPath();
        File targetFile = new File(targetFilePath);
        if (targetFile.exists()) {
            return false;
        }

        if (isZip) {
            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(file.toPath()))) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                while (zipEntry != null) {
                    String childPath = targetFilePath + separator + zipEntry.getName();
                    File childFile = new File(childPath);
                    if (!zipEntry.isDirectory()) {
                        File parentFile = childFile.getParentFile();
                        if (!parentFile.mkdirs() && !parentFile.exists()) {
                            throw new IOException("Could not create directory '" + parentFile + "'!");
                        }

                        Copier.copy(new BufferedInputStream(zipInputStream), childPath);
                    } else {
                        if (!childFile.mkdirs() && !childFile.exists()) {
                            throw new IOException("Could not create directory '" + childFile + "'!");
                        }
                    }

                    zipEntry = zipInputStream.getNextEntry();
                }
            }

            if (DPChecker.isDatapack(targetFilePath)) {
                main.getActiveDatapacks().put(targetFile.getName(), rootName);
                FileUtils.delete(file);
                return true;
            }
        }

        importEvent = false;
        Files.walkFileTree(Paths.get(targetFilePath), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String dirName = dir.getFileName().toString();
                if (dirName.equals("data") || dirName.equals("assets")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".zip")) {
                    new DPFinder(logger, main, rootName, separator).fileWalk(datapacksFolderPath, new File(String.valueOf(file)), true);
                    return FileVisitResult.CONTINUE;
                }

                Path parentPath = file.getParent();
                if (!fileName.equals("pack.mcmeta")
                        || !DPChecker.isDatapack(String.valueOf(parentPath))) {
                    return FileVisitResult.CONTINUE;
                }

                String parentName = parentPath.getFileName().toString();
                File datapackTargetFile = new File(
                        datapacksFolderPath + separator + parentName);
                if (datapackTargetFile.exists()) {
                    return FileVisitResult.CONTINUE;
                }

                FileUtils.copyDirectory(parentPath.toFile(), datapackTargetFile);
                HashMap<String, String> activeDatapacks = main.getActiveDatapacks();
                activeDatapacks.put(parentName, rootName);
                main.setActiveDatapacks(activeDatapacks);
                importEvent = true;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
                throw new IOException("IOException: Failed to visit file '" + file + "'!\n", e);
            }
        });

        FileUtils.deleteDirectory(targetFile);
        if (isZip) {
            FileUtils.delete(file);
        }

        return importEvent;
    }
}