package org.example.filestorageapi.errors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
//@Setter
@AllArgsConstructor
public class ErrorResponse {

    @JsonProperty("message")
    private String message;
}
