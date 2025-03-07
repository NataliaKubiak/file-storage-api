package org.example.filestorageapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.example.filestorageapi.dto.ResourceStreamResponseDto;
import org.example.filestorageapi.security.CustomUserDetails;
import org.example.filestorageapi.service.ResourceManagerService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

@Log4j2
@Controller
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

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

        // TODO: 07/03/2025 тут сразу приходит с user-X-files, путь до объекта который нажали скачать = "user-14-files/front/inside of front/test (1).txt"
        //закомментила в downloadResourceAsStream добавление user-X-files
        ResourceStreamResponseDto streamResponseDto = resourceManagerService.downloadResourceAsStream(path, userDetails.getId());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + streamResponseDto.getName())
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
            // TODO: 07/03/2025 тут вместо "file" как в задании - "object" приходит с фронта
            @RequestParam("object") List<MultipartFile> files,
            @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // TODO: 07/03/2025 путь без user-X-folder. загрузка из папки front, путь = "front/"
        //добавляю к пути user-X-files/ в последующей логике
        List<ResourceInfoResponseDto> resourceInfoList = resourceManagerService.uploadResources(files, path, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceInfoList);
    }

    /**
     * + 204 No Content
     * + 400 - невалидный или отсутствующий путь
     * + 401 - пользователь не авторизован
     * + 404 - ресурс не найден
     * + 500 - неизвестная ошибка
     */
    // path=$path
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // TODO: 07/03/2025 тут сразу приходит с user-X-files, путь до объекта который нажали удалить = "user-14-files/front/inside of front/test.txt"
        //закомментила в delete добавление user-X-files
        resourceManagerService.delete(path, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    /**
     * 200 OK
     * 400 - невалидный или отсутствующий путь
     * 401 - пользователь не авторизован
     * 404 - ресурс не найден
     * 409 - ресурс, лежащий по пути to уже существует
     * 500 - неизвестная ошибка
     */
    // /move?from=$from&to=$to
//    @GetMapping("/move")
//    public ResponseEntity<ResourceInfoResponseDto> renameOrMove(
//            @RequestParam String from,
//            @RequestParam String to,
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//
//        ResourceInfoResponseDto resourceInfoResponseDto = resourceManagerService.renameOrMove(from, to, userDetails.getId());
//
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(resourceInfoResponseDto);
//    }

    /**
     * + 200 OK
     * + 400 - невалидный или отсутствующий путь
     * + 401 - пользователь не авторизован
     * + 404 - ресурс не найден
     * + 500 - неизвестная ошибка
     */
    // path=$path
    @GetMapping
    public ResponseEntity<ResourceInfoResponseDto> getInfo(
            @RequestParam String path,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ResourceInfoResponseDto info = resourceManagerService.getInfo(path, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(info);
    }

    /**
     * + 200 OK
     * + 400 - невалидный или отсутствующий поисковый запрос
     * + 401 - пользователь не авторизован
     * + 500 - неизвестная ошибка
     */
    // /search?query=$query
    @GetMapping("/search")
    public ResponseEntity<List<ResourceInfoResponseDto>> search(
            @RequestParam String query,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // TODO: 07/03/2025 поисковый запрос "front", query = "front"
        // добавляю к пути user-X-files чтобы искать только среди объектов этого юзера
        List<ResourceInfoResponseDto> searchedResources = resourceManagerService.searchResources(query, userDetails.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(searchedResources);
    }
}
