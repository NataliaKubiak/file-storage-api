package org.example.filestorageapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.filestorageapi.utils.ResourceType;

@Getter
@AllArgsConstructor
@Builder
public class ResourceInfoResponseDto {
    /**
     * "path": "folder1/folder2", // путь к папке, в которой лежит ресурс
     * "name": "file.txt",
     * "size": 123, // размер файла в байтах. Если ресурс - папка, это поле отсутствует
     * "type": "DIRECTORY" // DIRECTORY или FILE
     */

    @JsonProperty("path")
    private String path;

    @JsonProperty("name")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @JsonProperty("size")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private long size;

    @JsonProperty("type")
    private ResourceType type;
}
