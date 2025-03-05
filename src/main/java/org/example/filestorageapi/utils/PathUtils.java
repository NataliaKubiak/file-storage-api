package org.example.filestorageapi.utils;

import lombok.experimental.UtilityClass;

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

    public static String addSlashToDirPath(String path) {
        return path.endsWith("/") ? path : path + "/";
    }
}
