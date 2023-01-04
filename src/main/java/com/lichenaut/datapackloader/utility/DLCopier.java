package com.lichenaut.datapackloader.utility;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;


public class DLCopier {

    public static void byteCopy(InputStream in, String outFilePath) throws IOException, NullPointerException {//avoid unnecessary overhead for small files by stream-ing bytes
        try (FileOutputStream out = new FileOutputStream(outFilePath)) {
            int len;while ((len = in.read()) != -1) {out.write((byte) len);}
        }
    }

    public static void copy(BufferedInputStream inputStream, String outFilePath, int copyType) throws IOException, NullPointerException {
        int capacity;
        if (copyType == 0) {//single file
            capacity = 4096;//4kb
        } else {//url file
            capacity = 1048576;//1mb
        }

        ReadableByteChannel in = Channels.newChannel(inputStream);
        WritableByteChannel out = Channels.newChannel(Files.newOutputStream(Paths.get(outFilePath)));
        ByteBuffer bBuffer = ByteBuffer.allocateDirect(capacity);
        while (in.read(bBuffer) != -1) {
            bBuffer.flip();
            out.write(bBuffer);
            bBuffer.clear();
        }
    }
}