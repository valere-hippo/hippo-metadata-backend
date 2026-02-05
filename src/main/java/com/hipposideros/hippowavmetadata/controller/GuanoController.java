package com.hipposideros.hippowavmetadata.controller;

import com.hipposideros.hippowavmetadata.service.GuanoService;
import com.hipposideros.hippowavmetadata.service.StorageService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guano")
public class GuanoController {

    private final StorageService storageService;
    private final GuanoService guanoService;

    public GuanoController(StorageService storageService, GuanoService guanoService) {
        this.storageService = storageService;
        this.guanoService = guanoService;
    }

    @GetMapping("/{fileId}")
    public Map<String, String> read(@PathVariable String fileId) {
        return guanoService.readGuano(storageService.resolve(fileId));
    }

    public record BatchUpdateRequest(@NotEmpty List<String> fileIds, @NotEmpty Map<String, String> updates) {}
    public record BatchDeleteRequest(@NotEmpty List<String> fileIds, @NotEmpty List<String> deleteKeys) {}

    @PostMapping("/batch-update")
    public void batchUpdate(@RequestBody BatchUpdateRequest req) {
        for (String fileId : req.fileIds()) {
            guanoService.updateGuano(storageService.resolve(fileId), req.updates());
        }
    }

    @PostMapping("/batch-delete")
    public Map<String, Map<String, String>> batchDelete(@RequestBody BatchDeleteRequest req) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();

        for (String fileId : req.fileIds()) {
            Path file = storageService.resolve(fileId);
            guanoService.deleteGuanoKeys(file, req.deleteKeys());
            result.put(fileId, guanoService.readGuano(file));
        }

        return result;
    }
}
