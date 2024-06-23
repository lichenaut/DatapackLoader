package com.lichenaut.datapackloader.dp;

import java.io.File;

public class Checker {

    public static boolean isDatapack(String filePath) {
        File[] files = new File(filePath).listFiles();
        if (files == null) {
            return false;
        }

        boolean hasPackMeta = false, hasData = false;
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.equals("data")) {
                hasPackMeta = true;
            } else if (fileName.equals("pack.mcmeta")) {
                hasData = true;
            }

            if (hasPackMeta && hasData) {
                break;
            }
        }

        return hasPackMeta && hasData;
    }
}