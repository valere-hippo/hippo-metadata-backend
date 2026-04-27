package com.hipposideros.hippowavmetadata.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StorageService {

    private final Path baseDir;
    private final GuanoService guanoService;

    public StorageService(@Value("${hippo.storage.base-dir}") String baseDir, GuanoService guanoService) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
        this.guanoService = guanoService;
    }

    public void init() throws IOException {
        Files.createDirectories(baseDir);
    }

    public List<String> saveAll(List<MultipartFile> files) throws IOException {
        init();

        List<String> ids = new ArrayList<>();

        for (MultipartFile file : files) {
            String original = file.getOriginalFilename();
            if (original == null || !original.toLowerCase().endsWith(".wav")) {
                throw new IllegalArgumentException("Only .wav files are allowed");
            }

            String baseName = original.substring(0, original.lastIndexOf('.'));
            String extension = ".wav";

            // 1️⃣ Upload temporaire avec UUID
            String tempId = UUID.randomUUID().toString();
            Path tempPath = baseDir.resolve(tempId + "-" + original);

            Files.copy(file.getInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);

            // 2️⃣ Lire GUANO
            Map<String, String> guano = guanoService.readGuano(tempPath);
            String timestamp = guano.get("Timestamp");

            // 3️⃣ Construire le nom final
            String finalName;
            if (timestamp != null && !timestamp.isBlank()) {
                String safeTs = formatTimestampForFilename(timestamp);
                finalName = baseName;
            } else {
                finalName = baseName;
            }

            Path finalPath = baseDir.resolve(finalName);

            // 4️⃣ Éviter collision
            int counter = 1;
            while (Files.exists(finalPath)) {
                finalPath = baseDir.resolve(
                        baseName + "_" + counter + extension
                );
                counter++;
            }

            // 5️⃣ Rename
            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);

            ids.add(finalPath.getFileName().toString());
        }

        return ids;
    }

    public Path resolve(String fileId) {
        return baseDir.resolve(fileId).toAbsolutePath().normalize();
    }

    public java.util.List<String> listAllFileIds() {
        try {
            init();
            try (var stream = java.nio.file.Files.list(baseDir)) {
                return stream
                        .filter(java.nio.file.Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .sorted()
                        .toList();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to list uploaded files", e);
        }
    }

    private String formatTimestampForFilename(String timestamp) {
        // ex: 2025-05-08 21:38:10
        return timestamp
                .replace(":", "-")
                .replace(" ", "_");
    }

}
