package org.example.filestorageapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.dto.UserAuthDto;
import org.example.filestorageapi.dto.UserResponseDto;
import org.example.filestorageapi.mapper.UserAuthDtoToUserMapper;
import org.example.filestorageapi.security.CustomUserDetails;
import org.example.filestorageapi.security.CustomUserDetailsService;
import org.example.filestorageapi.service.UserService;
import org.example.filestorageapi.swagger.CommonAuthResponses;
import org.example.filestorageapi.swagger.authController.SignInResponse;
import org.example.filestorageapi.swagger.authController.SignUpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Operations for user authentication and registration")
public class AuthController {

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    private final UserService userService;
    private final UserAuthDtoToUserMapper userMapper;

    @Operation(
            summary = "Sign in",
            description = "Authenticates a user and returns user information"
    )
    @SignInResponse
    @CommonAuthResponses
    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDto> signIn(@RequestBody @Valid UserAuthDto userAuthDto,
                                                  BindingResult bindingResult,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        hasValidationErrors(bindingResult);

        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(
                    userAuthDto.getUsername(),
                    userAuthDto.getPassword()
            );
            Authentication authentication = authenticationManager.authenticate(authRequest);

            if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
                log.warn("Authentication principal is not CustomUserDetails: {}",
                        authentication.getPrincipal().getClass().getName());
            }

            createAndSaveSecurityContext(request, response, authentication);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new UserResponseDto(userAuthDto.getUsername()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password.");
        }
    }

    @Operation(
            summary = "Sign up",
            description = "Registers a new user and returns user information"
    )
    @SignUpResponse
    @CommonAuthResponses
    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> performSignup(@RequestBody @Valid UserAuthDto userAuthDto,
                                                         BindingResult bindingResult,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
        hasValidationErrors(bindingResult);
        userService.registerUser(userMapper.toEntity(userAuthDto));

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userAuthDto.getUsername());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            createAndSaveSecurityContext(request, response, authentication);

        } catch (Exception e) {
            log.warn("Auto-login failed: {}", e.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new UserResponseDto(userAuthDto.getUsername()));
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
            throw new ValidationException(errorMsg.toString());
        }
    }

    private void createAndSaveSecurityContext(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        securityContextRepository.saveContext(securityContext, request, response);
    }
}
