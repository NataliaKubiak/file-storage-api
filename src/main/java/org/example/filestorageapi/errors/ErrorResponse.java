package org.example.filestorageapi.errors;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "Error information")
public class ErrorResponse {

    @JsonProperty("message")
    @Schema(description = "Error message", example = "Invalid username or password.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;
}
