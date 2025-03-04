package org.example.filestorageapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.dto.ResourceResponseDto;
import org.example.filestorageapi.service.MinioService;
import org.example.filestorageapi.utils.Validator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final MinioService minioService;

    /**
     * + 200 OK
     * + 400 - невалидный или отсутствующий путь
     * + 401 - пользователь не авторизован
     * + 404 - ресурс не найден
     * + 500 - неизвестная ошибка
     */
    // /download?path=$path
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadObject(@RequestParam String path) {
        Validator.validate(path);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        if (minioService.isFolder(path)) {
            String folderName = extractFilenameFromPath(path);
            headers.setContentDispositionFormData("attachment", folderName + ".zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(minioService.downloadFolderAsZipStream(path));

        } else {
            String filename = extractFilenameFromPath(path);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(minioService.downloadFileAsStream(path));
        }
    }

    private String extractFilenameFromPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            return path.substring(lastSlashIndex + 1);
        }
        return path;
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
    public ResponseEntity<List<ResourceResponseDto>> uploadFiles(
            @RequestParam("file") List<MultipartFile> files,
            @RequestParam String path) {
        Validator.validate(path);
        minioService.validateFolderExists(path);

        List<ResourceResponseDto> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            Validator.validate(file);

            ResourceResponseDto response = minioService.uploadFile(file, path, file.getContentType());
            responses.add(response);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responses);
    }

    // TODO: 03/03/2025 for testing!!!
    @PostMapping("/folder")
    public ResponseEntity<String> createFolder(@RequestParam("folderName") String folderName) {
        minioService.createFolder(folderName + "/");

        return ResponseEntity.ok("Folder uploaded successfully: " + folderName);
    }

    // TODO: 03/03/2025 for testing!!!
    @PostMapping("/init")
    public ResponseEntity<String> init() {
        minioService.init();
        return ResponseEntity.ok("MinIO initialized successfully");
    }
}
