package com.lichenaut.datapackloader.urlimport;

import com.lichenaut.datapackloader.DatapackLoader;
import com.lichenaut.datapackloader.utility.DLCopier;
import com.lichenaut.datapackloader.utility.DLDatapackChecker;
import com.lichenaut.datapackloader.utility.DLDirectoryMaker;
import com.lichenaut.datapackloader.utility.DLFileSeparatorGetter;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DLDatapackFinder extends SimpleFileVisitor<Path>{

    private final DatapackLoader plugin;
    private final String rootName;
    private boolean importEvent;

    public DLDatapackFinder(DatapackLoader plugin, String rootName) {this.plugin = plugin;this.rootName = rootName;}

    public boolean fileWalk(File file, boolean isZip) throws IOException {
        String targetFilePath = file.getPath();
        File targetFile = new File(targetFilePath);
        if (isZip) {
            targetFilePath = plugin.getDatapacksFolderPath() + DLFileSeparatorGetter.getSeparator() + file.getName().substring(0, file.getName().length()-4) + "z";
            targetFile = new File(targetFilePath);
            if (targetFile.exists()) {return false;}

            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(file.toPath()))) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                while (zipEntry != null) {
                    String childPath = targetFilePath + DLFileSeparatorGetter.getSeparator() + zipEntry.getName();
                    if (!zipEntry.isDirectory()) {DLCopier.copy(new BufferedInputStream(zipInputStream), childPath);} else {new DLDirectoryMaker(plugin).makeDir(childPath);}
                    zipEntry = zipInputStream.getNextEntry();
                }
            }

            if (DLDatapackChecker.isDatapack(targetFilePath)) {
                if (!targetFile.renameTo(new File(targetFilePath.substring(0, targetFilePath.length() - 1)))) {//remove 'z'
                    plugin.getLog().severe("Could not create remove 'z' from '" + targetFilePath + "'! SecurityException?");
                    throw new IOException();
                }
                plugin.getActiveDatapacks().put(targetFile.getName(), rootName);
                FileUtils.delete(file);
                return true;
            }
        }

        importEvent = false;
        Files.walkFileTree(Paths.get(targetFilePath), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.getFileName().toString().equals("data") || dir.getFileName().toString().equals("assets")) {return FileVisitResult.SKIP_SUBTREE;}return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                if (file.getFileName().toString().endsWith(".zip")) {new DLDatapackFinder(plugin, rootName).fileWalk(new File(String.valueOf(file)), true);return FileVisitResult.CONTINUE;}
                if (file.getFileName().toString().equals("pack.mcmeta")) {
                    if (DLDatapackChecker.isDatapack(String.valueOf(file.getParent()))) {
                        String datapackTarget = plugin.getDatapacksFolderPath() + DLFileSeparatorGetter.getSeparator() + file.getParent().getFileName().toString();
                        File datapackTargetFile = new File(datapackTarget);
                        if (datapackTargetFile.exists()) {return FileVisitResult.CONTINUE;}
                        FileUtils.copyDirectory(file.getParent().toFile(), datapackTargetFile);
                        plugin.getActiveDatapacks().put(file.getParent().getFileName().toString(), rootName);
                        importEvent = true;
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                plugin.getLog().severe("IOException: Could not visit '" + file.toAbsolutePath() + "'! Stopping process.");
                exc.printStackTrace();
                return FileVisitResult.TERMINATE;
            }
        });
        FileUtils.deleteDirectory(targetFile);
        if (isZip) {FileUtils.delete(file);}
        return importEvent;
    }
}