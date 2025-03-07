package org.example.filestorageapi.service;

import lombok.RequiredArgsConstructor;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.example.filestorageapi.dto.ResourceStreamResponseDto;
import org.example.filestorageapi.errors.ResourceAlreadyExistsException;
import org.example.filestorageapi.errors.ResourceNotFoundException;
import org.example.filestorageapi.utils.PathUtils;
import org.example.filestorageapi.utils.ResourceType;
import org.example.filestorageapi.utils.Validator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceManagerService {

    private final MinioService minioService;

    public ResourceStreamResponseDto downloadResourceAsStream(String path) {
        Validator.validatePath(path);
//        String fullPath = PathUtils.getPathWithUserDir(path, userId);

        boolean isFolder = minioService.isFolderOrThrowNotFound(path);

        String filename = PathUtils.extractFilenameFromPath(path);
        if (isFolder) {

            return ResourceStreamResponseDto.builder()
                    .name(PathUtils.encode(filename) + ".zip")
                    .responseBody(minioService.downloadFolderAsZipStream(path))
                    .build();

        } else {
            return ResourceStreamResponseDto.builder()
                    .name(PathUtils.encode(filename))
                    .responseBody(minioService.downloadFileAsStream(path))
                    .build();
        }
    }

    public List<ResourceInfoResponseDto> uploadResources(List<MultipartFile> files, String path, long userId) {
        Validator.validatePath(path);
        Validator.validateFiles(files);

        String fullPath = PathUtils.getPathWithUserDir(path, userId);

        checkAllFoldersExist(fullPath);

        List<ResourceInfoResponseDto> resourceInfoList = new ArrayList<>();
        for (MultipartFile file : files) {
            String fixedFilename = file.getOriginalFilename().replace(":", "/");

            String fullFilename = fullPath + fixedFilename;
            String filePath = PathUtils.getParentDirectoryPath(fullFilename);
            String fileName = PathUtils.extractFilenameFromPath(fullFilename);

            if (minioService.isFileExist(fullFilename)) {
                throw new ResourceAlreadyExistsException("File '" + fileName + "' already exist in directory: " + filePath);
            }

            ResourceInfoResponseDto response = minioService.uploadFile(file, filePath, fileName);
            resourceInfoList.add(response);
        }

        return resourceInfoList;
    }

    public void delete(String path) {
        Validator.validatePath(path);
//        String fullPath = PathUtils.getPathWithUserDir(path, userId);
        String fullPath = path;

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

    public ResourceInfoResponseDto getInfo(String path) {
        Validator.validatePath(path);

//        String fullPath = PathUtils.getPathWithUserDir(path, userId);
        String fullPath = path;

        boolean isFolder = minioService.isFolderOrThrowNotFound(fullPath);

        if (isFolder) {
            return getFolderInfo(fullPath);

        } else {
            return getFileInfo(fullPath);
        }
    }

    public List<ResourceInfoResponseDto> getInfoList(String path, long userId) {
        Validator.validatePath(path);

        String fullPath = PathUtils.getPathWithUserDir(path, userId);

        checkAllFoldersExist(fullPath);

        return minioService.getInfoList(fullPath);
    }

    public ResourceInfoResponseDto createFolder(String path, long userId) {
        Validator.validatePath(path);

        String fullPath = PathUtils.getPathWithUserDir(path, userId);

        if (minioService.isFileExist(fullPath)) {
            throw new ResourceAlreadyExistsException("Folder '" + "' already exists");
        }

        String parentDir = PathUtils.getParentDirectoryPath(fullPath);
        checkAllFoldersExist(parentDir);

        minioService.createFolder(fullPath);

        String name = PathUtils.getObjectName(fullPath, true);
        String pathToParentDir = PathUtils.getParentDirectoryPath(fullPath);

        return ResourceInfoResponseDto.builder()
                .path(pathToParentDir)
                .name(name)
                .type(ResourceType.DIRECTORY)
                .build();
    }

    public List<ResourceInfoResponseDto> searchResources(String searchWord, long userId) {
        Validator.validateQuery(searchWord);

        String userFolderPath = PathUtils.getPathWithUserDir("", userId);

        return minioService.searchByName(searchWord, userFolderPath);
    }

    private void checkAllFoldersExist(String fullPath) {
        List<String> pathsToAllFolders = PathUtils.getPathsForAllFolders(fullPath);

        for (String folderPath : pathsToAllFolders) {
            if (!minioService.isFolderExists(folderPath)) {
                throw new ResourceNotFoundException("Folder not found: " + folderPath);
            }
        }
    }

    private ResourceInfoResponseDto getFileInfo(String fullPath) {
        String name = PathUtils.getObjectName(fullPath, false);
        String pathToParentDir = PathUtils.getParentDirectoryPath(fullPath);

        long fileSize = minioService.getFileSize(fullPath);

        return ResourceInfoResponseDto.builder()
                .path(pathToParentDir)
                .name(name)
                .size(fileSize)
                .type(ResourceType.FILE)
                .build();
    }

    private ResourceInfoResponseDto getFolderInfo(String fullPath) {
        String name = PathUtils.getObjectName(fullPath, true);
        String pathToParentDir = PathUtils.getParentDirectoryPath(fullPath);

        return ResourceInfoResponseDto.builder()
                .path(pathToParentDir)
                .name(name)
                .type(ResourceType.DIRECTORY)
                .build();
    }
}
