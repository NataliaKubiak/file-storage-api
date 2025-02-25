package org.example.filestorageapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
//@Setter
@AllArgsConstructor
public class UserResponseDto {

    @JsonProperty("username")
    private String username;
}
