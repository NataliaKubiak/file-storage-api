package org.example.filestorageapi.utils;

import lombok.experimental.UtilityClass;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class PathUtils {

    public static String extractFilenameFromPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            return path.substring(lastSlashIndex + 1);
        }
        return path;
    }

    public static String getFullPathWithUserDir(String path, int userId) {
        return String.format("user-%d-files/%s", userId, path);
    }

    public static String addSlashToDirPath(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    public static String removeSlashFromTheEnd(String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public static String encode(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }
}
