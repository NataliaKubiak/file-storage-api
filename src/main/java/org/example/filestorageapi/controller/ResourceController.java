package org.example.filestorageapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.example.filestorageapi.dto.ResourceStreamResponseDto;
import org.example.filestorageapi.security.CustomUserDetails;
import org.example.filestorageapi.service.MinioService;
import org.example.filestorageapi.service.ResourceManagerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Log4j2
@Controller
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final MinioService minioService;
    private final ResourceManagerService resourceManagerService;

    /**
     * + 200 OK
     * + 400 - невалидный или отсутствующий путь
     * + 401 - пользователь не авторизован
     * + 404 - ресурс не найден
     * + 500 - неизвестная ошибка
     */
    // /download?path=$path
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadObject(
            @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ResourceStreamResponseDto streamResponseDto = resourceManagerService.downloadResourceAsStream(path, userDetails.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", streamResponseDto.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .body(streamResponseDto.getResponseBody());
    }

    /**
     * + 201 Created
     * + 400 - невалидное тело запроса
     * + 404 - папка, в которую мы загружаем ресурс(ы) не существует
     * + 401 - пользователь не авторизован
     * + 500 - неизвестная ошибка
     */
    // path=$path
    @PostMapping()
    public ResponseEntity<List<ResourceInfoResponseDto>> uploadFiles(
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam String path) {

        List<ResourceInfoResponseDto> resourceInfoList = resourceManagerService.uploadResources(files, path);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceInfoList);
    }
}
