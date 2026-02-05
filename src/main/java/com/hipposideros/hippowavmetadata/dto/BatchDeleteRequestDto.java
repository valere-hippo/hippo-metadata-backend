package com.hipposideros.hippowavmetadata.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BatchDeleteRequestDto(
        @NotEmpty List<String> fileIds,
        @NotEmpty List<String> deleteKeys
) {}