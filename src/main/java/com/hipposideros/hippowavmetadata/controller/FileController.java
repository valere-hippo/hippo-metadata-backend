package com.hipposideros.hippowavmetadata.controller;

import com.hipposideros.hippowavmetadata.dto.UploadResponseDto;
import com.hipposideros.hippowavmetadata.dto.UploadedFileDto;
import com.hipposideros.hippowavmetadata.service.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final StorageService storageService;

    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponseDto upload(@RequestPart("files") List<MultipartFile> files) throws IOException {
        List<String> ids = storageService.saveAll(files);
        return new UploadResponseDto(ids);
    }

    @GetMapping
    public List<UploadedFileDto> listFiles() {
        return storageService.listAllFileIds().stream()
                .map(fileId -> new UploadedFileDto(fileId, fileId))
                .toList();
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable String fileId) {
        try {
            Path filePath = storageService.resolve(fileId);

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        try {
            Path filePath = storageService.resolve(fileId);
            Files.deleteIfExists(filePath);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

