package com.oath.maven.plugin.freemarker;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class EntityPropertiesProvider implements OutputGeneratorPropertiesProvider {

    private final File outputDir;

    private EntityPropertiesProvider(File outputDir) {
        this.outputDir = outputDir;
    }

    public static EntityPropertiesProvider create(File outputDir) {
        return new EntityPropertiesProvider(outputDir);
    }

    @Override
    public void providePropertiesFromFile(Path path, OutputGenerator.OutputGeneratorBuilder builder) {
        File entityFile = path.toFile();
        Map<String, Object> data = parseEntity(entityFile);
        builder.addDataModel(data);
    }

    private Map<String, Object> parseEntity(File entityDataFile) {
        Map<String, Object> dataModel = new HashMap<>();
        String fileName = entityDataFile.getName();
        dataModel.put("entityName", fileName.substring(0, fileName.length() - 5));
        return dataModel;
    }

}
