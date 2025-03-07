package org.example.filestorageapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.filestorageapi.utils.ResourceType;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "Information about a resource (file or directory)")
public class ResourceInfoResponseDto {

    @Schema(
            description = "Resource path (directory containing the resource)",
            example = "/user-files/documents/",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("path")
    private String path;

    @Schema(
            description = "Resource name",
            example = "document.txt",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("name")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @Schema(
            description = "Resource size in bytes (omitted for directories)",
            example = "1024",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @JsonProperty("size")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private long size;

    @Schema(
            description = "Resource type (FILE or DIRECTORY)",
            example = "FILE",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"FILE", "DIRECTORY"}
    )
    @JsonProperty("type")
    private ResourceType type;
}
