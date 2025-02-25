package org.example.filestorageapi.dto;

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

    @NotEmpty(message = "Username can't be empty.")
    @Size(min = 2, max = 100, message = "Username should be from 2 to 100 characters.")
    private String username;

    @Pattern(regexp = "^(?!.*\\s$).*", message = "Password cannot contain only spaces or has space in the end.")
    private String password;
}
