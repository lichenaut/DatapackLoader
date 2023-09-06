package com.lichenaut.datapackloader.utility;

import java.nio.file.FileSystems;

public class DLSep {

    public static String getSep() {return FileSystems.getDefault().getSeparator();}
}