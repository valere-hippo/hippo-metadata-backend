package com.hipposideros.hippowavmetadata.controller;

import com.hipposideros.hippowavmetadata.dto.BatchDeleteRequestDto;
import com.hipposideros.hippowavmetadata.dto.BatchUpdateRequestDto;
import com.hipposideros.hippowavmetadata.dto.FileMetadataDto;
import com.hipposideros.hippowavmetadata.dto.ReadMetadataRequestDto;
import com.hipposideros.hippowavmetadata.service.MetadataService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @PostMapping("/read")
    public List<FileMetadataDto> read(@Valid @RequestBody ReadMetadataRequestDto req) {
        return metadataService.readMetadata(req.fileIds());
    }

    @PostMapping("/batch-update")
    public void batchUpdate(@Valid @RequestBody BatchUpdateRequestDto req) {
        metadataService.batchUpdate(req.fileIds(), req.updates());
    }

    @PostMapping("/batch-delete")
    public void batchDelete(@Valid @RequestBody BatchDeleteRequestDto req) {
        metadataService.batchDelete(req.fileIds(), req.deleteKeys());
    }
}

