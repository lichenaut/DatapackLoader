package com.lichenaut.datapackloader.urlimport;

import com.lichenaut.datapackloader.DatapackLoader;
import com.lichenaut.datapackloader.utility.DLCopier;
import com.lichenaut.datapackloader.utility.DLDatapackChecker;
import com.lichenaut.datapackloader.utility.DLSep;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DLDatapackFinder extends SimpleFileVisitor<Path>{

    private final DatapackLoader plugin;
    private final String rootName;
    private boolean importEvent;

    public DLDatapackFinder(DatapackLoader plugin, String rootName) {this.plugin = plugin;this.rootName = rootName;}

    public boolean fileWalk(String datapacksFolderPath, File file, boolean isZip) throws IOException {
        String targetFilePath = file.getPath();
        File targetFile = new File(targetFilePath);
        if (isZip) {
            targetFilePath = datapacksFolderPath + DLSep.getSep() + file.getName().substring(0, file.getName().length()-4);
            targetFile = new File(targetFilePath);
            if (targetFile.exists()) return false;

            try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(file.toPath()))) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();
                while (zipEntry != null) {
                    String childPath = targetFilePath + DLSep.getSep() + zipEntry.getName();

                    File childFile = new File(childPath);
                    if (!zipEntry.isDirectory()) {
                        File parentFile = childFile.getParentFile();
                        if (!parentFile.exists()) parentFile.mkdirs();
                        DLCopier.copy(new BufferedInputStream(zipInputStream), childPath);
                    } else if (!childFile.exists()) childFile.mkdirs();
                    zipEntry = zipInputStream.getNextEntry();
                }
            }

            if (DLDatapackChecker.isDatapack(targetFilePath)) {
                plugin.getActiveDatapacks().put(targetFile.getName(), rootName);
                FileUtils.delete(file);
                return true;
            }
        }

        importEvent = false;
        Files.walkFileTree(Paths.get(targetFilePath), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (dir.getFileName().toString().equals("data") || dir.getFileName().toString().equals("assets")) return FileVisitResult.SKIP_SUBTREE;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                if (file.getFileName().toString().endsWith(".zip")) {new DLDatapackFinder(plugin, rootName).fileWalk(datapacksFolderPath, new File(String.valueOf(file)),
                        true);return FileVisitResult.CONTINUE;}

                Path parentPath = file.getParent();
                if (!file.getFileName().toString().equals("pack.mcmeta") || !DLDatapackChecker.isDatapack(String.valueOf(parentPath))) return FileVisitResult.CONTINUE;

                File datapackTargetFile = new File(datapacksFolderPath + DLSep.getSep() + parentPath.getFileName().toString());
                if (datapackTargetFile.exists()) return FileVisitResult.CONTINUE;

                FileUtils.copyDirectory(parentPath.toFile(), datapackTargetFile);
                plugin.getActiveDatapacks().put(parentPath.getFileName().toString(), rootName);
                importEvent = true;
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
        if (isZip) FileUtils.delete(file);
        return importEvent;
    }
}