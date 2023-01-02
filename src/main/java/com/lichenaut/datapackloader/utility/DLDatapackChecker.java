package com.lichenaut.datapackloader.utility;

import java.io.File;
import java.util.Objects;

public class DLDatapackChecker {

    public static boolean isDatapack(String filePath) {
        File dir = new File(filePath);
        return Objects.requireNonNull(dir.listFiles((dir1, name) -> name.equals("pack.mcmeta"))).length == 1 &&
                Objects.requireNonNull(dir.listFiles((dir1, name) -> name.equals("data"))).length == 1;
    }
}