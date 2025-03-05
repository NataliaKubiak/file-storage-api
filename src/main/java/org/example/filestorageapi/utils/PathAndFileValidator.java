package org.example.filestorageapi.utils;

import jakarta.validation.ValidationException;
import lombok.experimental.UtilityClass;
import org.example.filestorageapi.errors.InvalidPathException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@UtilityClass
public class PathAndFileValidator {

    public static void validatePath(String path) {
        if (path == null || path.isEmpty()) {
            throw new InvalidPathException("Path cannot be empty");
        }

        if (path.contains("..")) {
            throw new InvalidPathException("Path cannot contain parent directory references (..)");
        }

        String disallowedChars = "<>:\"|?*";
        for (char c : disallowedChars.toCharArray()) {
            if (path.indexOf(c) >= 0) {
                throw new InvalidPathException("Path contains disallowed character: '" + c + "'");
            }
        }

        if (path.length() > 200) {
            throw new InvalidPathException("Path length exceeds maximum of 200 characters");
        }

        if (path.contains("//") || path.contains("\\\\")) {
            throw new InvalidPathException("Path cannot have repeated slashes (// or \\\\)");
        }
    }

    public static void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new ValidationException("Uploaded files are empty");
        }

        long maxSize = 10 * 1024 * 1024; // 10MB

        for (MultipartFile file : files) {
            if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
                throw new ValidationException("File name can't be empty or consist of spaces");
            }

            if (file.getSize() > maxSize) {
                throw new ValidationException("File '" + file.getOriginalFilename() + "' size exceeds the 10MB limit");
            }
        }
    }
}
