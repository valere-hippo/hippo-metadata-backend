package com.hipposideros.hippowavmetadata.service;

import org.springframework.stereotype.Service;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiffChunkService {

    public record ChunkInfo(String id, long size, long dataOffset) {}

    public List<ChunkInfo> listChunks(Path wavFile) {
        try (RandomAccessFile raf = new RandomAccessFile(wavFile.toFile(), "r")) {

            byte[] riff = new byte[4];
            raf.readFully(riff);
            if (!"RIFF".equals(new String(riff, StandardCharsets.US_ASCII))) {
                throw new IllegalArgumentException("Not a RIFF file");
            }

            int riffSize = Integer.reverseBytes(raf.readInt());

            byte[] wave = new byte[4];
            raf.readFully(wave);
            if (!"WAVE".equals(new String(wave, StandardCharsets.US_ASCII))) {
                throw new IllegalArgumentException("Not a WAVE file");
            }

            long fileEnd = riffSize + 8L;

            List<ChunkInfo> chunks = new ArrayList<>();
            while (raf.getFilePointer() < fileEnd) {
                byte[] idBytes = new byte[4];
                raf.readFully(idBytes);
                String chunkId = new String(idBytes, StandardCharsets.US_ASCII);

                int sizeLE = Integer.reverseBytes(raf.readInt());
                long size = sizeLE & 0xffffffffL;

                long dataOffset = raf.getFilePointer();
                chunks.add(new ChunkInfo(chunkId, size, dataOffset));

                long skip = size + (size % 2); // padding
                raf.seek(dataOffset + skip);
            }

            return chunks;
        } catch (Exception e) {
            throw new RuntimeException("Failed to list RIFF chunks for " + wavFile.getFileName(), e);
        }
    }
}
