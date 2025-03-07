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
                responseCode = "200",
                description = "OK",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        array = @ArraySchema(schema = @Schema(implementation = ResourceInfoResponseDto.class)),
                        examples = @ExampleObject(
                                name = "Directory information",
                                value = """
                                        [
                                            {
                                                "path": "user-6-files/folder1/folder2/",
                                                "name": "new portrait.png",
                                                "size": 1043912,
                                                "type": "FILE"
                                            },
                                            {
                                                "path": "user-6-files/folder1/folder2/",
                                                "name": "pictures.zip",
                                                "size": 22,
                                                "type": "FILE"
                                            },
                                            {
                                                "path": "user-6-files/folder1/folder2/",
                                                "name": "test.txt",
                                                "size": 17,
                                                "type": "FILE"
                                            }
                                        ]
                                        """
                        )
                )
        )
})
public @interface DirectoryInfoResponse {
}
