package com.hipposideros.hippowavmetadata.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public record BatchUpdateRequestDto(
        @NotEmpty List<String> fileIds,
        @NotEmpty Map<String, String> updates
) {}