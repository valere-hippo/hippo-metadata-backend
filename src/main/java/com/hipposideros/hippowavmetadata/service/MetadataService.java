package com.hipposideros.hippowavmetadata.service;

import com.hipposideros.hippowavmetadata.dto.FileMetadataDto;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class MetadataService {

    private final StorageService storageService;
    private final ExifToolService exifToolService;
    private final GuanoPythonService guanoPythonService;

    public MetadataService(StorageService storageService, ExifToolService exifToolService, GuanoPythonService guanoPythonService) {
        this.storageService = storageService;
        this.exifToolService = exifToolService;
        this.guanoPythonService = guanoPythonService;
    }

    public List<FileMetadataDto> readMetadata(List<String> fileIds) {
        return fileIds.stream().map(fileId -> {
            Path path = storageService.resolve(fileId);
            JsonNode metadata = exifToolService.readMetadata(path);
            return new FileMetadataDto(fileId, path.getFileName().toString(), toMap(metadata));
        }).toList();
    }

    public void batchUpdate(List<String> fileIds, Map<String, String> updates) {
        for (String fileId : fileIds) {
            Path path = storageService.resolve(fileId);
            guanoPythonService.updateGuano(path, updates);
          //  exifToolService.batchUpdate(path, updates);
        }
    }

    public void batchDelete(List<String> fileIds, List<String> deleteKeys) {
        for (String fileId : fileIds) {
            Path path = storageService.resolve(fileId);
            guanoPythonService.deleteGuano(path, deleteKeys);
           // exifToolService.batchDelete(path, deleteKeys);
        }
    }

    private Map<String, Object> toMap(JsonNode node) {
        return new ObjectMapper().convertValue(node, Map.class);
    }
}
