package com.lichenaut.datapackloader.utility;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


public class DLCopier {

    public static void byteCopy(InputStream in, String outFilePath) throws IOException, NullPointerException {//avoid unnecessary overhead for small files by stream-ing bytes
        try (FileOutputStream out = new FileOutputStream(outFilePath)) {
            int len;
            while ((len = in.read()) != -1) {out.write((byte) len);}
        }
    }

    public static void copy(BufferedInputStream inputStream, String outFilePath) throws IOException, NullPointerException {
        ReadableByteChannel in = Channels.newChannel(inputStream);
        try (FileOutputStream fileOutputStream = new FileOutputStream(outFilePath)) {fileOutputStream.getChannel().transferFrom(in, 0, Long.MAX_VALUE);}
    }
}