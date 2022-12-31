package com.lichenaut.datapackloader.utility;

import java.io.File;
import java.util.Objects;

public class DLDatapackChecker {

    public static boolean isDatapack(String filePath) {
        boolean hasData = false;
        boolean hasMeta = false;
        for (File sibling : Objects.requireNonNull(new File(filePath).listFiles())) {
            if (sibling.isDirectory() && sibling.getName().equals("data")) {hasData = true;continue;}
            if (!sibling.isDirectory() && sibling.getName().equals("pack.mcmeta")) {hasMeta = true;}
        }
        return hasData && hasMeta;
    }
}