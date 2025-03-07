package org.example.filestorageapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDto {

    @Schema(
            description = "Username of the authenticated user",
            example = "johndoe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("username")
    private String username;
}
