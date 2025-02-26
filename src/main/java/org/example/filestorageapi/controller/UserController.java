package org.example.filestorageapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.dto.UserResponseDto;
import org.example.filestorageapi.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    /**
     * + 200 ok
     * + 401 - пользователь не авторизован
     * + 500 - неизвестная ошибка
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new UserResponseDto(userDetails.getUsername()));
    }
}
