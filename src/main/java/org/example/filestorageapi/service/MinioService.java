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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Log4j2
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    public void createFolder(String folderPath) {
        try {
            folderPath = PathUtils.addSlashToTheEnd(folderPath);

            putObject(folderPath, new ByteArrayInputStream(new byte[0]), 0, "application/x-directory");

        } catch (Exception e) {
            log.error("Error creating folder: {}", e.getMessage());
            throw new RuntimeException("Could not create folder");
        }
    }

    private Iterable<Result<Item>> listAllObjectsInDir(String folderPath) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(folderPath)
                        .recursive(true)
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

    private void statObject(String filePath) throws Exception {
        minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath)
                        .build());
    }

    // refactored
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
            log.error("Error initializing MinIO: {}", e.getMessage());
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
                    throw new ResourceNotFoundException("File or Directory doesn't exist: " + path);
                }

                throw new RuntimeException("Unexpected error while accessing MinIO");
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error while accessing MinIO");
            }
        }
    }

    public boolean isFolderExists(String path) {
        String normalizedPath = PathUtils.addSlashToTheEnd(path);

        Iterable<Result<Item>> results = listFirstObjectInDir(normalizedPath);

        return results.iterator().hasNext();
    }

    public boolean isFileExist(String filePath) {
        // TODO: 06/03/2025 надо ли нам тут удалять слеш в конце или мы уверены что слеша не будет?
        String normalizedPath = PathUtils.removeSlashFromTheEnd(filePath);

        try {
            statObject(normalizedPath);

            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw new RuntimeException("Error checking if file exists", e);

        } catch (Exception e) {
            throw new RuntimeException("Error checking if file exists", e);
        }
    }


    public StreamingResponseBody downloadFolderAsZipStream(String folderPath) {
        folderPath = PathUtils.addSlashToTheEnd(folderPath);

        final String finalFolderPath = folderPath;

        return outputStream -> {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                Iterable<Result<Item>> results = listAllObjectsInDir(finalFolderPath);

                for (Result<Item> result : results) {
                    Item item;
                    try {
                        item = result.get();
                    } catch (Exception e) {
                        continue;
                    }

                    String objectName = item.objectName();
                    if (objectName.endsWith("/")) {
                        continue;
                    }

                    String entryName = objectName.substring(finalFolderPath.length());

                    try {
                        zipOut.putNextEntry(new ZipEntry(entryName));

                        InputStream objectStream = getObject(objectName);

                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = objectStream.read(buffer)) != -1) {
                            zipOut.write(buffer, 0, bytesRead);
                        }

                        zipOut.closeEntry();
                        objectStream.close();
                    } catch (Exception e) {
                        log.error("Error adding file to ZIP: {}", objectName, e);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to create ZIP stream: {}", e.getMessage(), e);
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

            return ResourceInfoResponseDto.builder()
                    .path(path)
                    .name(fileName)
                    .size(file.getSize())
                    .type(ResourceType.FILE)
                    .build();
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Unexpected error. Could not upload file");
        }
    }
}
