package com.hipposideros.hippowavmetadata.dto;

import java.util.Map;

public record FileMetadataDto(
        String fileId,
        String filename,
        Map<String, Object> metadata
) {}