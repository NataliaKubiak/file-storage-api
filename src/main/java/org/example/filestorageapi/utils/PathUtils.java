package org.example.filestorageapi.utils;

import lombok.experimental.UtilityClass;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@UtilityClass
public class PathUtils {

    public static String addSlashToTheEnd(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    public static String removeSlashFromTheEnd(String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public static String encode(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    public static boolean hasSlashInTheEnd(String string) {
        return string.endsWith("/");
    }

    public static String getPathWithUserDir(String path, int userId) {
        if (path.isEmpty()) {
            return String.format("user-%d-files/", userId);
        } else {
            return String.format("user-%d-files/%s", userId, path);
        }
    }

    public String getParentDirectoryPath(String pathString) {
        boolean endsWithSlash = pathString.endsWith("/");

        if (endsWithSlash) {
            pathString = pathString.substring(0, pathString.length() - 1);
        }

        int lastSlashIndex = pathString.lastIndexOf("/");

        if (lastSlashIndex == -1) {
            return "";
        }

        return pathString.substring(0, lastSlashIndex + 1);
    }

    public String getObjectName(String pathString, boolean isFolder) {
        String workingPath = pathString;
        if (workingPath.endsWith("/")) {
            workingPath = workingPath.substring(0, workingPath.length() - 1);
        }

        int lastSlashIndex = workingPath.lastIndexOf("/");

        String lastComponent;
        if (lastSlashIndex == -1) {
            lastComponent = workingPath;
        } else {
            lastComponent = workingPath.substring(lastSlashIndex + 1);
        }

        if (isFolder) {
            return lastComponent + "/";
        } else {
            return lastComponent;
        }
    }

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

    public static ArrayList<String> getPathsForAllFolders(String path) {
        String[] folders = path.split("/");
        ArrayList<String> result = new ArrayList<>();

        StringBuilder currentPath = new StringBuilder();
        for (int i = 0; i < folders.length; i++) {
            if (i > 0) {
                currentPath.append("/");
            }
            currentPath.append(folders[i]);
            result.add(currentPath.toString());
        }
        return result;
    }
}
