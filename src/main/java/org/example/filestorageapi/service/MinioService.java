package org.example.filestorageapi.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.filestorageapi.dto.ResourceInfoResponseDto;
import org.example.filestorageapi.errors.ResourceNotFoundException;
import org.example.filestorageapi.utils.PathUtils;
import org.example.filestorageapi.utils.ResourceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Log4j2
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @PostConstruct
    public void init() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.debug("Bucket '{}' created successfully", bucketName);
            } else {
                log.debug("Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO: {}", e.getMessage(), e);
            throw new RuntimeException("Could not initialize MinIO", e);
        }
    }

    public boolean isFolderOrThrowNotFound(String path) {
        String folderPath = PathUtils.addSlashToTheEnd(path);

        Iterable<Result<Item>> results = listFirstObjectInDir(folderPath);
        boolean hasResults = results.iterator().hasNext();

        if (hasResults) {
            return true;
        } else {
            try {
                String filePath = PathUtils.removeSlashFromTheEnd(path);
                statObject(filePath);

                return false;
            } catch (ErrorResponseException e) {
                if (e.errorResponse().code().equals("NoSuchKey")) {
                    log.warn("File or directory '{}' does not exist", path);
                    throw new ResourceNotFoundException("File or Directory doesn't exist: " + path);
                }

                log.error("Unexpected error while accessing MinIO: {}", e.getMessage(), e);
                throw new RuntimeException("Unexpected error while accessing MinIO", e);
            } catch (Exception e) {
                log.error("Unexpected error while accessing MinIO: {}", e.getMessage(), e);
                throw new RuntimeException("Unexpected error while accessing MinIO", e);
            }
        }
    }

    public boolean isFolderExists(String path) {
        String normalizedPath = PathUtils.addSlashToTheEnd(path);
        Iterable<Result<Item>> results = listFirstObjectInDir(normalizedPath);

        return results.iterator().hasNext();
    }

    public boolean isFileExist(String filePath) {
        try {
            statObject(filePath);
            return true;

        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {

                return false;
            }
            log.error("Error checking if file exists: {}", e.getMessage(), e);
            throw new RuntimeException("Error checking if file exists", e);
        } catch (Exception e) {
            log.error("Error checking if file exists: {}", e.getMessage(), e);
            throw new RuntimeException("Error checking if file exists", e);
        }
    }

    public StreamingResponseBody downloadFolderAsZipStream(String folderPath) {
        String normalizedPath = PathUtils.addSlashToTheEnd(folderPath);

        return outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                addFolderContentsToZip(normalizedPath, zipOut);

            } catch (Exception e) {
                log.error("Failed to create ZIP stream for folder {}: {}", normalizedPath, e.getMessage(), e);
                throw new RuntimeException("Failed to generate ZIP archive");
            }
        };
    }

    public StreamingResponseBody downloadFileAsStream(String rawPath) {
        return outputStream -> {
            try (InputStream fileStream = getObject(rawPath)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fileStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

            } catch (Exception e) {
                log.error("Error downloading file: {}", e.getMessage());
                throw new RuntimeException("Could not download file");
            }
        };
    }

    public ResourceInfoResponseDto uploadFile(MultipartFile file, String path, String fileName) {
        try {
            putObject(path + fileName, file.getInputStream(), file.getSize(), file.getContentType());

            log.info("File '{}' uploaded to: {}", fileName, path);
            return ResourceInfoResponseDto.builder()
                    .path(path)
                    .name(fileName)
                    .size(file.getSize())
                    .type(ResourceType.FILE)
                    .build();
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Unexpected error. Could not upload file: " + path + fileName);
        }
    }

    public void deleteFile(String path) {
        try {
            removeObject(path);
            log.info("File '{}' deleted successfully", path);

        } catch (Exception e) {
            log.error("Error deleting file '{}': {}", path, e.getMessage(), e);
            throw new RuntimeException("Unexpected error. Could not delete file: " + path, e);
        }
    }

    public void deleteFolder(String folderPath) {
        String normalizedPath = PathUtils.addSlashToTheEnd(folderPath);

        try {
            Iterable<Result<Item>> objects = listAllObjectsInDir(normalizedPath, true);
            for (Result<Item> result : objects) {
                removeObject(result.get().objectName());
            }
            log.info("Folder '{}' deleted successfully", folderPath);

        } catch (Exception e) {
            log.error("Error deleting folder '{}': {}", folderPath, e.getMessage(), e);
            throw new RuntimeException("Unexpected error. Could not delete folder: " + folderPath, e);
        }
    }

    public long getFileSize(String path) {
        try {
            StatObjectResponse fileInfo = statObject(path);

            return fileInfo.size();

        } catch (Exception e) {
            log.error("Error getting file info: {}", e.getMessage());
            throw new RuntimeException("Unexpected error. Could not get file info: " + path);
        }
    }

    public List<ResourceInfoResponseDto> searchByName(String searchWord, String userFolder) {
        List<ResourceInfoResponseDto> matchingItems = new ArrayList<>();
        log.info("Searching for '{}' in folder: {}", searchWord, userFolder);

        try {
            Iterable<Result<Item>> results = listAllObjectsInDir(userFolder, true);
            int processedItems = 0;
            String lowerCaseSearchWord = searchWord.toLowerCase();

            for (Result<Item> result : results) {
                try {
                    Item item = result.get();
                    processedItems++;

                    String objectName = PathUtils.getObjectName(
                            item.objectName(),
                            PathUtils.hasSlashInTheEnd(item.objectName())
                    );

                    if (objectName.toLowerCase().contains(lowerCaseSearchWord)) {
                        ResourceInfoResponseDto itemInfo = createResourceInfoDto(item);
                        matchingItems.add(itemInfo);
                    }

                } catch (Exception e) {
                    log.warn("Skipping unreadable item during search in {}: {}", userFolder, e.getMessage());
                }
            }

            log.info("Found {} matches for '{}' in folder {} (searched through {} items)",
                    matchingItems.size(), searchWord, userFolder, processedItems);
        } catch (Exception e) {
            log.error("Error searching for '{}' in folder {}: {}", searchWord, userFolder, e.getMessage(), e);
            throw new RuntimeException("Error searching for items in MinIO");
        }

        return matchingItems;
    }

    public List<ResourceInfoResponseDto> getInfoList(String fullPath) {
        List<ResourceInfoResponseDto> infoList = new ArrayList<>();

        try {
            Iterable<Result<Item>> results = listAllObjectsInDir(fullPath, false);
            int itemCount = 0;

            for (Result<Item> result : results) {
                try {
                    Item item = result.get();
                    ResourceInfoResponseDto itemInfo = createResourceInfoDto(item);

                    infoList.add(itemInfo);
                    itemCount++;
                } catch (Exception e) {
                    log.warn("Skipping unreadable item in directory {}: {}", fullPath, e.getMessage());
                }
            }

            log.info("Retrieved {} items from path {}", itemCount, fullPath);
        } catch (Exception e) {
            log.error("Error getting info for items in path {}: {}", fullPath, e.getMessage(), e);
            throw new RuntimeException("Error getting info for items in MinIO");
        }

        return infoList;
    }

    public void createFolder(String folderPath) {
        try {
            putObject(folderPath, new ByteArrayInputStream(new byte[0]), 0, "application/x-directory");

            log.info("Folder '{}' created", folderPath);
        } catch (Exception e) {
            log.error("Error creating folder: {}", e.getMessage());
            throw new RuntimeException("Could not create folder");
        }
    }

    private ResourceInfoResponseDto createResourceInfoDto(Item item) {
        String objectFullName = item.objectName();
        boolean isFolder = PathUtils.hasSlashInTheEnd(objectFullName);

        String path = PathUtils.getParentDirectoryPath(objectFullName);
        String objectName = PathUtils.getObjectName(objectFullName, isFolder);

        return ResourceInfoResponseDto.builder()
                .path(path)
                .name(objectName)
                .size(isFolder ? 0 : item.size())
                .type(isFolder ? ResourceType.DIRECTORY : ResourceType.FILE)
                .build();
    }

    private void addFolderContentsToZip(String folderPath, ZipOutputStream zipOut) throws Exception {
        Iterable<Result<Item>> results = listAllObjectsInDir(folderPath, true);

        for (Result<Item> result : results) {
            Item item;
            try {
                item = result.get();
            } catch (Exception e) {
                log.warn("Skipping unreadable item in folder {}", folderPath, e);
                continue;
            }

            String objectName = item.objectName();
            if (objectName.endsWith("/")) {
                continue;
            }

            String entryName = objectName.substring(folderPath.length());
            try {
                zipOut.putNextEntry(new ZipEntry(entryName));

                try (InputStream objectStream = getObject(objectName)) {
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = objectStream.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, bytesRead);
                    }
                }

                zipOut.closeEntry();
            } catch (Exception e) {
                log.error("Error adding file {} to ZIP", objectName, e);
            }
        }
    }

    private Iterable<Result<Item>> listAllObjectsInDir(String folderPath, boolean isRecursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(folderPath)
                        .recursive(isRecursive)
                        .build());
    }

    private Iterable<Result<Item>> listFirstObjectInDir(String folderPath) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(folderPath)
                        .maxKeys(1)
                        .build());
    }

    private InputStream getObject(String path) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .build());
    }

    private void putObject(String fullPath, InputStream inputStream, long objectSize, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fullPath)
                        .stream(inputStream, objectSize, -1)
                        .contentType(contentType)
                        .build());
    }

    private StatObjectResponse statObject(String filePath) throws Exception {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath)
                        .build());
    }

    private void removeObject(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }
}
