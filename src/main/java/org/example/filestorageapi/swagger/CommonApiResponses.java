package org.example.filestorageapi.swagger;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.filestorageapi.errors.ErrorResponse;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Invalid path",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"message\":\"Path cannot have repeated slashes (// or \\\\)\"}"
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "User not authorized",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"message\":\"Authentication required\"}"
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "File or directory not found",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"message\":\"File or directory 'pictures/folder2/' does not exist\"}"
                        )
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Unknown or unexpected error",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"message\":\"An unexpected error occurred while processing your request\"}"
                        )
                )
        )
})
public @interface CommonApiResponses {
}
