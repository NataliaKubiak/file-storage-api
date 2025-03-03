package org.example.filestorageapi.utils;

import lombok.experimental.UtilityClass;
import org.example.filestorageapi.errors.InvalidPathException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class PathUtils {

    public static String decodeAndValidate(String encodedPath) {
        String decodedPath = decode(encodedPath);
        validate(decodedPath);
        return decodedPath;
    }

    public static String decode(String encodedPath) {
        if (encodedPath == null || encodedPath.isEmpty()) {
            throw new InvalidPathException("Path cannot be empty");
        }

        try {
            return URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new InvalidPathException("Invalid URL encoding: " + e.getMessage());
        }
    }

    public static void validate(String path) {
        //если validate использовать без decode - то надо все равно проверять на null
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
}
