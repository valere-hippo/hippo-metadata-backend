package com.hipposideros.hippowavmetadata.controller;

import com.hipposideros.hippowavmetadata.service.RiffChunkService;
import com.hipposideros.hippowavmetadata.service.StorageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riff")
public class RiffController {

    private final StorageService storageService;
    private final RiffChunkService riffChunkService;

    public RiffController(StorageService storageService, RiffChunkService riffChunkService) {
        this.storageService = storageService;
        this.riffChunkService = riffChunkService;
    }

    @GetMapping("/chunks/{fileId}")
    public List<RiffChunkService.ChunkInfo> chunks(@PathVariable String fileId) {
        return riffChunkService.listChunks(storageService.resolve(fileId));
    }
}
