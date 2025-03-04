package org.example.filestorageapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.example.filestorageapi.utils.ResourceType;

@Getter
@AllArgsConstructor
@Builder
public class ResourceResponseDto {
    /**
     * "path": "folder1/folder2", // путь к папке, в которой лежит ресурс
     * "name": "file.txt",
     * "size": 123, // размер файла в байтах. Если ресурс - папка, это поле отсутствует
     * "type": "DIRECTORY" // DIRECTORY или FILE
     */

    @JsonProperty("path")
    private String path;

    @JsonProperty("name")
    private String name;

    @JsonProperty("size")
    private long size;

    @JsonProperty("type")
    private ResourceType type;
}
