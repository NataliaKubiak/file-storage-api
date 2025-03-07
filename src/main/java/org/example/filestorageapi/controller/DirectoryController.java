package org.example.filestorageapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.example.filestorageapi.errors.ExceptionUtils;
import org.example.filestorageapi.security.CustomUserDetails;
import org.example.filestorageapi.service.ResourceManagerService;
import org.example.filestorageapi.swagger.CommonApiResponses;
import org.example.filestorageapi.swagger.directoryController.CreateDirectoryResponse;
import org.example.filestorageapi.swagger.directoryController.DirectoryInfoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Log4j2
@Controller
@RequestMapping("/api/directory")
@RequiredArgsConstructor
@Tag(name = "Directory Management", description = "Operations for managing directories")
@SecurityRequirement(name = "cookieAuth")
public class DirectoryController {

    private final ResourceManagerService resourceManagerService;

    @Operation(
            summary = "Get directory information",
            description = "Returns information about all resources in the specified directory"
    )
    @DirectoryInfoResponse
    @CommonApiResponses
    @GetMapping
    public ResponseEntity<List<ResourceInfoResponseDto>> getInfo(
            @Parameter(description = "Directory path", required = true) @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ExceptionUtils.ifSessionExpiredThrowException(userDetails);

        // TODO: 07/03/2025 тут приходит "" из папки юзера и "front/" (те без user-X-files)
        //добавляю к пути user-X-files/ в последующей логике
        List<ResourceInfoResponseDto> infoList = resourceManagerService.getInfoList(path, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(infoList);
    }

    @Operation(
            summary = "Create new folder",
            description = "Creates a new folder at the specified path"
    )
    @CreateDirectoryResponse
    @CommonApiResponses
    @PostMapping()
    public ResponseEntity<ResourceInfoResponseDto> createFolder(
            @Parameter(description = "Folder path", required = true) @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ExceptionUtils.ifSessionExpiredThrowException(userDetails);

        // TODO: 07/03/2025 тут приходит без user-X-files и с / в конце
        //добавляю к пути user-X-files/ в последующей логике
        ResourceInfoResponseDto folderInfo = resourceManagerService.createFolder(path, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(folderInfo);
    }
}
