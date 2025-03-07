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
                responseCode = "200",
                description = "Successfully authenticated",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = UserResponseDto.class),
                        examples = @ExampleObject(
                                value = "{\"username\":\"johndoe\"}"
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Authentication failed",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = "{\"message\":\"Invalid username or password.\"}"
                        )
                )
        )
})
public @interface SignInResponse {
}
