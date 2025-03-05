package org.example.filestorageapi.service;

import lombok.RequiredArgsConstructor;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.example.filestorageapi.dto.ResourceStreamResponseDto;
import org.example.filestorageapi.utils.PathUtils;
import org.example.filestorageapi.utils.PathAndFileValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceManagerService {

    private final MinioService minioService;

    public List<ResourceInfoResponseDto> uploadResources(List<MultipartFile> files, String path) {
        PathAndFileValidator.validatePath(path);
        PathAndFileValidator.validateFiles(files);

        minioService.validateFolderExists(path);

        List<ResourceInfoResponseDto> resourceInfoList = new ArrayList<>();
        for (MultipartFile file : files) {

            ResourceInfoResponseDto response = minioService.uploadFile(file, path, file.getContentType());
            resourceInfoList.add(response);
        }

        return resourceInfoList;
    }

    public ResourceStreamResponseDto downloadResourceAsStream(String path, int userId) {
        PathAndFileValidator.validatePath(path);
        String fullPath = PathUtils.getFullPathWithUserDir(path, userId);

        boolean isFolder = minioService.isFolderOrThrowNotFound(fullPath);

        String filename = PathUtils.extractFilenameFromPath(fullPath);
        if (isFolder) {

            return ResourceStreamResponseDto.builder()
                    .name(PathUtils.encode(filename) + ".zip")
                    .responseBody(minioService.downloadFolderAsZipStream(fullPath))
                    .build();

        } else {
            return ResourceStreamResponseDto.builder()
                    .name(PathUtils.extractFilenameFromPath(PathUtils.encode(filename)))
                    .responseBody(minioService.downloadFileAsStream(fullPath))
                    .build();
        }
    }
}
