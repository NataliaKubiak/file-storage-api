package org.example.filestorageapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.filestorageapi.dto.UserAuthDto;
import org.example.filestorageapi.dto.UserResponseDto;
import org.example.filestorageapi.errors.UserAlreadyExistException;
import org.example.filestorageapi.mapper.UserAuthDtoToUserMapper;
import org.example.filestorageapi.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAuthDtoToUserMapper userMapper;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository securityContextRepository;

    /**
     * + 201 Created
     * + 400 - ошибки валидации (пример - слишком короткий username)
     * + 409 - username занят
     * + 500 - неизвестная ошибка
     */
    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> performSignup(@RequestBody @Valid UserAuthDto userAuthDto,
                                                         BindingResult bindingResult) {
        hasValidationErrors(bindingResult);

        String username = userAuthDto.getUsername();

        if (userService.findUserByUsername(username).isPresent()) {
            throw new UserAlreadyExistException("User with name '" + username + "' already exists.");
        }

        // TODO: 24/02/2025 Не сделано: При регистрации юзеру сразу создаётся сессия и выставляется кука
        userService.registerUser(userMapper.toEntity(userAuthDto));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserResponseDto(username));
    }

    /**
     * + 200 OK
     * + 400 - ошибки валидации (пример - слишком короткий username)
     * + 401 - неверные данные (такого пользователя нет, или пароль неправильный)
     * + 500 - неизвестная ошибка
     */
    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDto> performSignin(@RequestBody @Valid UserAuthDto userAuthDto,
                                                         BindingResult bindingResult,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {

        hasValidationErrors(bindingResult);
        String username = userAuthDto.getUsername();

        Authentication authenticationRequest =
                new UsernamePasswordAuthenticationToken(username, userAuthDto.getPassword());

        Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResponse);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new UserResponseDto(username));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout() {

        SecurityContextHolder.clearContext();
        // TODO: 24/02/2025 что нужно сделать перед выходом? сессии + Redis как-то использовать?

        return ResponseEntity.noContent().build();
    }

    private void hasValidationErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();

            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMsg.append(error.getField())
                        .append(" - ").append(error.getDefaultMessage())
                        .append("; ");
            }
            throw new IllegalArgumentException(errorMsg.toString());
        }
    }
}
