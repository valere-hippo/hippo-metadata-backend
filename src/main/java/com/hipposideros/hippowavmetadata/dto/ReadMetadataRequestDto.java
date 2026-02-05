package com.hipposideros.hippowavmetadata.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReadMetadataRequestDto(
        @NotEmpty List<String> fileIds
) {}