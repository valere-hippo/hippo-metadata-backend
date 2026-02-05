package com.hipposideros.hippowavmetadata.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExifToolService {

    private final String exifToolPath;
    private final ObjectMapper objectMapper;

    public ExifToolService(@Value("${hippo.exiftool.path}") String exifToolPath,
                           ObjectMapper objectMapper) {
        this.exifToolPath = exifToolPath;
        this.objectMapper = objectMapper;
    }

    public JsonNode readMetadata(Path wavFile) {
        try {
            List<String> cmd = List.of(
                    exifToolPath,
                    "-json",
                    "-a",
                    "-G1",
                    "-s",
                    wavFile.toString()
            );

            String output = run(cmd);
            JsonNode arr = objectMapper.readTree(output);
            return arr.get(0); // first object
        } catch (Exception e) {
            throw new RuntimeException("Failed to read metadata: " + wavFile.getFileName(), e);
        }
    }

    public void batchUpdate(Path wavFile, Map<String, String> updates) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(exifToolPath);
            cmd.add("-overwrite_original");

            // updates ex: {"CreateDate":"2025:05:08 21:38:10"}
            for (var entry : updates.entrySet()) {
                cmd.add("-" + entry.getKey() + "=" + entry.getValue());
            }

            cmd.add(wavFile.toString());
            run(cmd);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update metadata: " + wavFile.getFileName(), e);
        }
    }

    public void batchDelete(Path wavFile, List<String> deleteKeys) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(exifToolPath);
            cmd.add("-overwrite_original");

            for (String key : deleteKeys) {
                cmd.add("-" + key + "="); // delete
            }

            cmd.add(wavFile.toString());
            run(cmd);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete metadata: " + wavFile.getFileName(), e);
        }
    }

    private String run(List<String> command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process p = pb.start();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }

        int code = p.waitFor();
        if (code != 0) {
            throw new RuntimeException("ExifTool failed: " + sb);
        }
        return sb.toString();
    }
}
