package com.lichenaut.datapackloader.utility;

import java.io.File;

public class DLDatapackChecker {

    public static boolean isDatapack(String filePath) {
        File dir = new File(filePath);
        File[] files = dir.listFiles();
        if (files == null) return false;

        boolean hasPackMeta = false, hasData = false;
        for (File file : files) {
            if (file.getName().equals("pack.mcmeta")) hasPackMeta = true; else if (file.getName().equals("data")) hasData = true;
            if (hasPackMeta && hasData) break;
        }

        return hasPackMeta && hasData;
    }
}