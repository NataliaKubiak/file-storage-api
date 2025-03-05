package org.example.filestorageapi.service;

import lombok.RequiredArgsConstructor;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.example.filestorageapi.dto.ResourceStreamResponseDto;
import org.example.filestorageapi.utils.PathUtils;
import org.example.filestorageapi.utils.Validator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceManagerService {

    private final MinioService minioService;

    public List<ResourceInfoResponseDto> uploadResources(List<MultipartFile> files, String path) {
        Validator.decodeAndValidateUrlPath(path);
        Validator.validateFiles(files);

        minioService.validateFolderExists(path);

        List<ResourceInfoResponseDto> resourceInfoList = new ArrayList<>();
        for (MultipartFile file : files) {

            ResourceInfoResponseDto response = minioService.uploadFile(file, path, file.getContentType());
            resourceInfoList.add(response);
        }

        return resourceInfoList;
    }

    public ResourceStreamResponseDto downloadResourceAsStream(String path, int userId) {
        Validator.decodeAndValidateUrlPath(path);

        if (minioService.isFolder(path)) {
            return ResourceStreamResponseDto.builder()
                    .name(PathUtils.extractFilenameFromPath(path) + ".zip")
                    .responseBody(minioService.downloadFolderAsZipStream(path))
                    .build();

        } else {
            return ResourceStreamResponseDto.builder()
                    .name(PathUtils.extractFilenameFromPath(path))
                    .responseBody(minioService.downloadFileAsStream(path))
                    .build();
        }
    }
}
