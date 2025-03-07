package org.example.filestorageapi.swagger.authController;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.filestorageapi.dto.UserResponseDto;
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
                responseCode = "201",
                description = "User successfully created",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = UserResponseDto.class),
                        examples = @ExampleObject(
                                value = "{\"username\":\"johndoe\"}"
                        )
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Username already taken",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"message\":\"User with name 'johndoe' already exists.\"}"
                        )
                )
        )
})
public @interface SignUpResponse {
}
