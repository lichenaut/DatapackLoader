package com.lichenaut.datapackloader.utility;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


public class DLCopier {

    public static void smallCopy(InputStream in, String outFilePath) throws IOException {
        try (BufferedInputStream bufferedIn = new BufferedInputStream(in); FileOutputStream out = new FileOutputStream(outFilePath); BufferedOutputStream bufferedOut = new BufferedOutputStream(out)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bufferedIn.read(buffer)) != -1) bufferedOut.write(buffer, 0, bytesRead);
        }
    }

    public static void copy(BufferedInputStream inputStream, String outFilePath) throws IOException {
        ReadableByteChannel in = Channels.newChannel(inputStream);
        try (FileOutputStream fileOutputStream = new FileOutputStream(outFilePath)) {fileOutputStream.getChannel().transferFrom(in, 0, Long.MAX_VALUE);}
    }
}