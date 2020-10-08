// Copyright 2018, Oath Inc.
// Licensed under the terms of the Apache 2.0 license. See the LICENSE file in the project root for terms.

package com.oath.maven.plugin.freemarker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import freemarker.template.Configuration;
import freemarker.template.Version;

/**
 * Simple utility class to call various constructors.
 * Needed because some jmockit features don't work well with constructors.
 */
public class FactoryUtil {

    public static Configuration createConfiguration(String freeMarkerVersion) {
        return new Configuration(new Version(freeMarkerVersion));
    }

    public static File createFile(File parent, String child) {
        return new File(parent, child);
    }

    public static FileInputStream createFileInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public static File createFile(String name) {
        return new File(name);
    }

    public static List<Path> dirToPaths(File dir) {
        File[] fileArray = dir.listFiles();
        if (fileArray != null) {
            return Arrays.stream(fileArray)
                    .map(File::toPath)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static List<File> pathsToFiles(List<Path> paths) {
        return paths.stream()
                .map(Path::toFile)
                .collect(Collectors.toList());
    }

    public static Map<File, File> createFilesFromNamesInDir(String prefix,
                                                            List<File> postfixFiles, Path dir) {
        Map<File, File> map = new HashMap<>();
        for (File postfixFile : postfixFiles) {
            String fileName = prefix + postfixFile.getName() + ".java";
            File file = createFile(dir.toFile(), fileName);
            map.put(postfixFile, file);
        }
        return map;
    }

    public static List<String> getFileNames(List<File> Files) {
        return Files.stream()
                .map(File::getName)
                .collect(Collectors.toList());
    }
}
