package org.example.filestorageapi.service;

import lombok.RequiredArgsConstructor;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.example.filestorageapi.dto.ResourceStreamResponseDto;
import org.example.filestorageapi.errors.ResourceAlreadyExistsException;
import org.example.filestorageapi.errors.ResourceNotFoundException;
import org.example.filestorageapi.utils.PathAndFileValidator;
import org.example.filestorageapi.utils.PathUtils;
import org.example.filestorageapi.utils.ResourceType;
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
            if (!minioService.isFolderExists(folderPath)) {
                throw new ResourceNotFoundException("Folder not found: " + folderPath);
            }
        }

        List<ResourceInfoResponseDto> resourceInfoList = new ArrayList<>();
        for (MultipartFile file : files) {
            String fixedFilename = file.getOriginalFilename().replace(":", "/");

            String fullFilename = fullPath + fixedFilename;
            String filePath = PathUtils.getPathForFile(fullFilename);
            String fileName = PathUtils.extractFilenameFromPath(fullFilename);

            if (minioService.isFileExist(fullFilename)) {
                throw new ResourceAlreadyExistsException("File '" + fileName + "' already exist in directory: " + filePath);
            }

            ResourceInfoResponseDto response = minioService.uploadFile(file, filePath, fileName);
            resourceInfoList.add(response);
        }

        return resourceInfoList;
    }

    public void delete(String path, int userId) {
        PathAndFileValidator.validatePath(path);
        String fullPath = PathUtils.getFullPathWithUserDir(path, userId);

        boolean isFolder = minioService.isFolderOrThrowNotFound(fullPath);

        if (isFolder) {
            minioService.deleteFolder(fullPath);
        } else {
            minioService.deleteFile(fullPath);
        }
    }

    /**
     * При переименовании меняется только имя файла
     * При перемещении меняется только путь к файлу
     */
//    public ResourceInfoResponseDto renameOrMove(String from, String to, int userId) {
        //validate from & to FORMAT 400

        //check from -> if not ok -> not found 404
        //RENAME check to -> if not ok -> already exist 409
        //MOVE check to -> inf not ok -> not found 404

//        if (isFolder) {
        //rename or move folder

//        return ResourceInfoResponseDto.builder()
//                .path()
//                .name()
//                .type(ResourceType.DIRECTORY)
//                .build();

//        } else {
        //rename or move file

//        return ResourceInfoResponseDto.builder()
//                .path()
//                .name()
//                .size()
//                .type(ResourceType.FILE)
//                .build();
//        }
//    }

    public ResourceInfoResponseDto getInfo(String path, int userId) {
        PathAndFileValidator.validatePath(path);

        String fullPath = PathUtils.getFullPathWithUserDir(path, userId);

        boolean isFolder = minioService.isFolderOrThrowNotFound(fullPath);

        if (isFolder) {
            return ResourceInfoResponseDto.builder()
                    .path(path)
                    // TODO: 06/03/2025 какое имя показывается если папка?
//                .name()
                    .type(ResourceType.DIRECTORY)
                    .build();

        } else {
            String filePath = PathUtils.getPathForFile(path);
            String fileName = PathUtils.extractFilenameFromPath(path);

            long fileSize = minioService.getFileSize(fullPath);

            return ResourceInfoResponseDto.builder()
                    .path(filePath)
                    .name(fileName)
                    .size(fileSize)
                    .type(ResourceType.FILE)
                    .build();
        }
    }
}
