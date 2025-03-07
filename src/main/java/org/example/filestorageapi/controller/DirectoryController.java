package org.example.filestorageapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.example.filestorageapi.security.CustomUserDetails;
import org.example.filestorageapi.service.ResourceManagerService;
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
public class DirectoryController {

    private final ResourceManagerService resourceManagerService;

    /**
     * 200 OK
     * 400 - невалидный или отсутствующий путь
     * 401 - пользователь не авторизован
     * 404 - папка не существует
     * 500 - неизвестная ошибка
     */
//     path=$path
    @GetMapping
    public ResponseEntity<List<ResourceInfoResponseDto>> getInfo(
            @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // TODO: 07/03/2025 тут приходит "" из папки юзера и "front/" (те без user-X-files)
        //добавляю к пути user-X-files/ в последующей логике
        List<ResourceInfoResponseDto> infoList = resourceManagerService.getInfoList(path, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(infoList);
    }

    //?path=$path
    @PostMapping()
    public ResponseEntity<ResourceInfoResponseDto> createFolder(
            @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // TODO: 07/03/2025 тут приходит без user-X-files и с / в конце
        //добавляю к пути user-X-files/ в последующей логике
        ResourceInfoResponseDto folderInfo = resourceManagerService.createFolder(path, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(folderInfo);
    }
}
