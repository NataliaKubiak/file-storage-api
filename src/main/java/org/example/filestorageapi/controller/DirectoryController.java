package org.example.filestorageapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.service.MinioService;
import org.example.filestorageapi.utils.Validator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@Controller
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController {

    private final MinioService minioService;

    //?path=$path
    @PostMapping()
    public ResponseEntity<String> createFolder(@RequestParam String path) {
        Validator.decodeAndValidateUrlPath(path);




        minioService.createFolder(path + "/");

        return ResponseEntity.ok("Folder uploaded successfully: " + path);
    }
}
