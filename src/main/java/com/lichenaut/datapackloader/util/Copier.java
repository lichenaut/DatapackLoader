package com.lichenaut.datapackloader.util;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class Copier {

    public static void copy(BufferedInputStream inputStream, String outFilePath) throws IOException {
        ReadableByteChannel in = Channels.newChannel(inputStream);
        try (FileOutputStream fileOutputStream = new FileOutputStream(outFilePath)) {
            fileOutputStream.getChannel().transferFrom(in, 0, Long.MAX_VALUE);
        }
    }
}