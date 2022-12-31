package com.lichenaut.datapackloader.utility;

import java.nio.file.FileSystems;

public class DLFileSeparatorGetter {

    public static String getSeparator() {return FileSystems.getDefault().getSeparator();}
}