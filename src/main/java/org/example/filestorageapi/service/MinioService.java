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

    public boolean isFolder(String path) {
        String normalizedPath = PathUtils.addSlashToDirPath(path);

        Iterable<Result<Item>> results = listFirstObjectInDir(normalizedPath);
        boolean hasResults = results.iterator().hasNext();

        if (hasResults) {
            return true;
        } else {
            try {
                minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucketName)
                                .object(path.endsWith("/") ? path.substring(0, path.length() - 1) : path)
                                .build()
                );

                return false;
            } catch (ErrorResponseException e) {
                if (e.errorResponse().code().equals("NoSuchKey")) {
                    throw new ResourceNotFoundException("File or Directory doesn't exist");
                }

                throw new RuntimeException("Unexpected error while accessing MinIO");
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error while accessing MinIO");
            }
        }
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
                throw new RuntimeException("Could not download file", e);
            }
        };
    }

    public StreamingResponseBody downloadFolderAsZipStream(String folderPath) {
        folderPath = PathUtils.addSlashToDirPath(folderPath);

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

    public void createFolder(String folderPath) {
        try {
            folderPath = PathUtils.addSlashToDirPath(folderPath);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(folderPath)
                            .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                            .build());
        } catch (Exception e) {
            log.error("Error creating folder: {}", e.getMessage());
            throw new RuntimeException("Could not create folder");
        }
    }

    public ResourceInfoResponseDto uploadFile(MultipartFile file, String urlPath, String contentType) {
        urlPath = PathUtils.addSlashToDirPath(urlPath);

        String fixedFilename = file.getOriginalFilename().replace(":", "/");
        String fullFilename = urlPath + fixedFilename;

        String fileName = fixedFilename;
        String filePath = urlPath;

        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);

            String subdirs = fixedFilename.substring(0, fixedFilename.lastIndexOf("/"));
            filePath = urlPath + subdirs;
        }

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fullFilename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build());

            return ResourceInfoResponseDto.builder()
                    .path(filePath.endsWith("/") ? filePath.substring(0, filePath.length() - 1) : filePath)
                    .name(fileName)
                    .size(file.getSize())
                    .type(ResourceType.FILE)
                    .build();
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Unexpected error. Could not upload file");
        }
    }

    public void validateFolderExists(String path) {
        String normalizedPath = PathUtils.addSlashToDirPath(path);

        Iterable<Result<Item>> results = listFirstObjectInDir(normalizedPath);

        if (!results.iterator().hasNext()) {
            throw new ResourceNotFoundException("Folder not found: " + path);
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
}
