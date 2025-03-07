package org.example.filestorageapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.dto.UserResponseDto;
import org.example.filestorageapi.errors.ExceptionUtils;
import org.example.filestorageapi.security.CustomUserDetails;
import org.example.filestorageapi.swagger.userController.UserInfoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Operations for managing user information")
@SecurityRequirement(name = "cookieAuth")
public class UserController {

    @Operation(
            summary = "Get current user information",
            description = "Returns information about the currently authenticated user"
    )
    @UserInfoResponse
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ExceptionUtils.ifSessionExpiredThrowException(userDetails);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new UserResponseDto(userDetails.getUsername()));
    }
}
