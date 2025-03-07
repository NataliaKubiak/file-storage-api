package org.example.filestorageapi.swagger.directoryController;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "CREATED",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        array = @ArraySchema(schema = @Schema(implementation = ResourceInfoResponseDto.class)),
                        examples = @ExampleObject(
                                name = "Information about created folder",
                                value = """
                                        {
                                            "path": "user-6-files/folder1/",
                                            "name": "folder2/",
                                            "type": "DIRECTORY"
                                        }
                                        """
                        )
                )
        )
})
public @interface CreateDirectoryResponse {
}
