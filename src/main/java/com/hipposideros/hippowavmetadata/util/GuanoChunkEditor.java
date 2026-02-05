package com.hipposideros.hippowavmetadata.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GuanoChunkEditor {

    public static void replaceGuano(Path wav, String newGuanoText) throws IOException {
        Path tmp = Files.createTempFile("wav-guano-", ".wav");

        try (RandomAccessFile in = new RandomAccessFile(wav.toFile(), "r");
             RandomAccessFile out = new RandomAccessFile(tmp.toFile(), "rw")) {

            byte[] header = new byte[12];
            in.readFully(header);
            out.write(header);

            while (in.getFilePointer() < in.length()) {
                byte[] id = new byte[4];
                in.readFully(id);
                String chunkId = new String(id, StandardCharsets.US_ASCII);

                int size = Integer.reverseBytes(in.readInt());
                byte[] data = new byte[size];
                in.readFully(data);

                if ("guan".equals(chunkId)) {
                    byte[] newData = newGuanoText.getBytes(StandardCharsets.UTF_8);
                    out.write("guan".getBytes(StandardCharsets.US_ASCII));
                    out.writeInt(Integer.reverseBytes(newData.length));
                    out.write(newData);
                } else {
                    out.write(id);
                    out.writeInt(Integer.reverseBytes(size));
                    out.write(data);
                }

                if (size % 2 == 1) {
                    in.readByte();
                    out.write(0);
                }
            }
        }

        Files.move(tmp, wav, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }
}
