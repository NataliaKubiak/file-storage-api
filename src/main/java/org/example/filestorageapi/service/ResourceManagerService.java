package org.example.filestorageapi.service;

import lombok.RequiredArgsConstructor;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.example.filestorageapi.dto.ResourceStreamResponseDto;
import org.example.filestorageapi.utils.PathAndFileValidator;
import org.example.filestorageapi.utils.PathUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceManagerService {

    private final MinioService minioService;

    public ResourceStreamResponseDto downloadResourceAsStream(String path, int userId) {
        PathAndFileValidator.validatePath(path);
        String fullPath = PathUtils.getFullPathWithUserDir(path, userId);

        boolean isFolder = minioService.isFolderOrThrowNotFound(fullPath);
        //начинается с PathUtils.addSlashToTheEnd(path);

        String filename = PathUtils.extractFilenameFromPath(fullPath);
        if (isFolder) {

            return ResourceStreamResponseDto.builder()
                    .name(PathUtils.encode(filename) + ".zip")
                    .responseBody(minioService.downloadFolderAsZipStream(fullPath))
                    //начинается с PathUtils.addSlashToTheEnd(path);
                    .build();

        } else {
            return ResourceStreamResponseDto.builder()
                    .name(PathUtils.extractFilenameFromPath(PathUtils.encode(filename)))
                    .responseBody(minioService.downloadFileAsStream(fullPath))
                    .build();
        }
    }

    public List<ResourceInfoResponseDto> uploadResources(List<MultipartFile> files, String path, int userId) {
        PathAndFileValidator.validatePath(path);
        PathAndFileValidator.validateFiles(files);

        String fullPath = PathUtils.getFullPathWithUserDir(path, userId);
        fullPath = PathUtils.addSlashToTheEnd(fullPath);

        List<String> pathsToAllFolders = PathUtils.getPathsForAllFolders(fullPath);

        for (String folderPath : pathsToAllFolders) {
            minioService.validateFolderExists(folderPath);
            //начинается с PathUtils.addSlashToTheEnd(path);
        }

        List<ResourceInfoResponseDto> resourceInfoList = new ArrayList<>();
        for (MultipartFile file : files) {
            String fixedFilename = file.getOriginalFilename().replace(":", "/");

            String fullFilename = fullPath + fixedFilename;
            String filePath = PathUtils.getPathForFile(fullFilename);
            String fileName = PathUtils.extractFilenameFromPath(fullFilename);

            ResourceInfoResponseDto response = minioService.uploadFile(file, filePath, fileName);
            resourceInfoList.add(response);
        }

        return resourceInfoList;
    }
}
