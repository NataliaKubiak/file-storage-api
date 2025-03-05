package org.example.filestorageapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Getter
@AllArgsConstructor
@Builder
public class ResourceStreamResponseDto {

    private String name;

    private StreamingResponseBody responseBody;
}
