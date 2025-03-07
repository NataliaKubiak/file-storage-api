package org.example.filestorageapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAuthDto {

    @Schema(
            description = "User's username for authentication",
            example = "johndoe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "Username can't be empty.")
    @Size(min = 2, max = 100, message = "Username should be from 2 to 100 characters.")
    private String username;

    @Schema(
            description = "User's password",
            example = "mySecureP@ssw0rd",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password"
    )
    @Pattern(regexp = "^(?!.*\\s$).*", message = "Password cannot contain only spaces or has space in the end.")
    private String password;
}
