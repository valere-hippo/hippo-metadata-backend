package com.hipposideros.hippowavmetadata.service;

import org.springframework.stereotype.Service;

import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@Service
public class GuanoService {

    private final RiffChunkService riffChunkService;

    public GuanoService(RiffChunkService riffChunkService) {
        this.riffChunkService = riffChunkService;
    }

    public Map<String, String> readGuano(Path wavFile) {
        var chunk = findGuanoChunk(wavFile);
        if (chunk == null) {
            return Map.of(); // no GUANO
        }

        try (RandomAccessFile raf = new RandomAccessFile(wavFile.toFile(), "r")) {
            raf.seek(chunk.dataOffset());

            byte[] data = new byte[(int) chunk.size()];
            raf.readFully(data);

            String text = new String(data, StandardCharsets.UTF_8).trim();
            return parseGuano(text);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read GUANO from " + wavFile.getFileName(), e);
        }
    }

    public void updateGuano(Path wavFile, Map<String, String> updates) {
        Map<String, String> existing = new LinkedHashMap<>(readGuano(wavFile));
        existing.putAll(updates);

        String newText = serializeGuano(existing);
        replaceGuanoChunk(wavFile, newText);
    }

    public void deleteGuanoKeys(Path wavFile, List<String> deleteKeys) {
        Map<String, String> before = readGuano(wavFile);
        System.out.println("GUANO BEFORE delete: " + before);

        Map<String, String> existing = new LinkedHashMap<>(before);
        for (String k : deleteKeys) {
            existing.remove(k);
        }

        System.out.println("GUANO AFTER delete map: " + existing);

        String newText = serializeGuano(existing);
        System.out.println("GUANO TEXT TO WRITE:\n" + newText);

        replaceGuanoChunk(wavFile, newText);

        Map<String, String> after = readGuano(wavFile);
        System.out.println("GUANO AFTER rewrite: " + after);
    }

    // ---------------- internal helpers ----------------

    private RiffChunkService.ChunkInfo findGuanoChunk(Path wavFile) {
        return riffChunkService.listChunks(wavFile).stream()
                .filter(c -> "guan".equals(c.id()))
                .findFirst()
                .orElse(null);
    }

    private Map<String, String> parseGuano(String text) {
        Map<String, String> map = new LinkedHashMap<>();

        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // ignore header line
            if (line.startsWith("GUANO|")) continue;

            int idx = line.indexOf(':');
            if (idx <= 0) continue;

            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            map.put(key, value);
        }

        return map;
    }

    private String serializeGuano(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("GUANO|Version: 1.0\n");
        for (var e : map.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Safe approach: rewrite entire WAV file and replace only the 'guan' chunk.
     */
    private void replaceGuanoChunk(Path wavFile, String newGuanoText) {
        Path tmp;
        try {
            tmp = Files.createTempFile("hippo-guano-", ".wav");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create temp file", e);
        }

        byte[] newData = newGuanoText.getBytes(StandardCharsets.UTF_8);

        try (RandomAccessFile in = new RandomAccessFile(wavFile.toFile(), "r");
             RandomAccessFile out = new RandomAccessFile(tmp.toFile(), "rw")) {

            // Copy RIFF header (12 bytes)
            byte[] header = new byte[12];
            in.readFully(header);
            out.write(header);

            while (in.getFilePointer() < in.length()) {

                // read chunk header
                byte[] idBytes = new byte[4];
                int r = in.read(idBytes);
                if (r < 4) break;

                String chunkId = new String(idBytes, StandardCharsets.US_ASCII);

                int sizeLE = in.readInt();
                int size = Integer.reverseBytes(sizeLE);

                // read chunk data
                byte[] data = new byte[size];
                in.readFully(data);

                // read padding byte if needed
                if (size % 2 == 1) {
                    in.readByte();
                }

                if ("guan".equals(chunkId)) {
                    // write updated GUANO chunk
                    out.write("guan".getBytes(StandardCharsets.US_ASCII));
                    out.writeInt(Integer.reverseBytes(newData.length));
                    out.write(newData);

                    if (newData.length % 2 == 1) {
                        out.write(0);
                    }
                } else {
                    // write original chunk unchanged
                    out.write(idBytes);
                    out.writeInt(Integer.reverseBytes(size));
                    out.write(data);

                    if (size % 2 == 1) {
                        out.write(0);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to update GUANO chunk for " + wavFile.getFileName(), e);
        }

        try {
            fixRiffHeaderSize(tmp);
            Files.move(tmp, wavFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to replace original WAV file", e);
        }
    }

    private void fixRiffHeaderSize(Path wavFile) {
        try (RandomAccessFile raf = new RandomAccessFile(wavFile.toFile(), "rw")) {
            long fileSize = raf.length();
            long riffSize = fileSize - 8; // RIFF size = file size - 8
            raf.seek(4);
            raf.writeInt(Integer.reverseBytes((int) riffSize));
        } catch (Exception e) {
            throw new RuntimeException("Failed to fix RIFF header size", e);
        }
    }

}
