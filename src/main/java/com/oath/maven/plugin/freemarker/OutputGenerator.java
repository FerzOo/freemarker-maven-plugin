// Copyright 2018, Oath Inc.
// Licensed under the terms of the Apache 2.0 license. See the LICENSE file in the project root for terms.

package com.oath.maven.plugin.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Knows how to generate an output file given five things:
 * <ul>
 * <li>The latest update time of the <b>pom file(s)</b> for the project</li>
 * <li>The location of the <b>generator file</b></li>
 * <li>The location of the <b>template file</b></li>
 * <li>The location of the <b>output file</b></li>
 * <li>A <b>data model<b> used to fill out the template.</li>
 * </ul>
 * <p>Given these five pieces of information, the generator will generate a new output file, but only if any existing
 * generated file is not newer than the inputs (pom, generator, and template).</p>
 */
class OutputGenerator {
    public final long pomModifiedTimestamp;
    public final Path generatorLocation;
    public final List<Path> templateLocations;
    public final Path outputDir;
    public final Map<String, Object> dataModel;

    private OutputGenerator(
            long pomModifiedTimestamp,
            Path generatorLocation,
            List<Path> templateLocations,
            Path outputDir,
            Map<String, Object> dataModel) {
        this.pomModifiedTimestamp = pomModifiedTimestamp;
        this.generatorLocation = generatorLocation;
        this.templateLocations = templateLocations;
        this.outputDir = outputDir;
        this.dataModel = dataModel;
    }

    /**
     * Uses a fluent builder to make the code more legible in place.
     * Also allows the output generator to be built from multiple locations in source by passing the builder.
     *
     * @return A new fluent builder for the OutputGenerator class.
     */
    public static OutputGeneratorBuilder builder() {
        return new OutputGeneratorBuilder();
    }

    public static class OutputGeneratorBuilder {
        private long pomModifiedTimestamp = Long.MAX_VALUE;
        private Path generatorLocation = null;
        private List<Path> templateLocations = null;
        private Path outputDir = null;
        private Map<String, Object> dataModel = null;

        public OutputGeneratorBuilder addPomLastModifiedTimestamp(long pomModifiedTimestamp) {
            this.pomModifiedTimestamp = pomModifiedTimestamp;
            return this;
        }

        public OutputGeneratorBuilder addGeneratorLocation(Path generatorLocation) {
            this.generatorLocation = generatorLocation;
            return this;
        }

        public OutputGeneratorBuilder addTemplateLocations(List<Path> templateLocations) {
            this.templateLocations = templateLocations;
            return this;
        }

        public OutputGeneratorBuilder addOutDir(Path outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public OutputGeneratorBuilder addDataModel(Map<String, Object> dataModel) {
            this.dataModel = dataModel;
            return this;
        }

        public OutputGeneratorBuilder addToDataModel(String key, Object val) {
            if (this.dataModel == null) {
                this.dataModel = new HashMap<>(4);
            }
            this.dataModel.put(key, val);
            return this;
        }

        /**
         * @return A new output generator (which is immutable).
         * @throws IllegalStateException if any of the parts of the OutputGenerator were not set.
         */
        public OutputGenerator create() {
            if (pomModifiedTimestamp == Long.MAX_VALUE)
                throw new IllegalStateException("Must set the pomModifiedTimestamp");
            if (generatorLocation == null) throw new IllegalStateException("Must set a non-null generatorLocation");
            if (templateLocations == null) throw new IllegalStateException("Must set a non-null templateLocation");
            if (outputDir == null) throw new IllegalStateException("Must set a non-null outputLocation");
            if (dataModel == null) throw new IllegalStateException("Must set a non-null dataModel");
            return new OutputGenerator(pomModifiedTimestamp, generatorLocation, templateLocations, outputDir, dataModel);
        }
    }

    /**
     * <p>Generates an output by applying the model to the template.</p>
     * <p>Checks the ages of the inputs against an existing output file to early exit if there is no update.</p>
     *
     * @param config Used to load the template from the template name.
     */
    public void generate(Configuration config) {
        //Use "createFile" for testing purposes only
        File generatorFile = generatorLocation.toFile();
        List<File> templateFiles = FactoryUtil.pathsToFiles(templateLocations);
        Map<File, File> templateToOutputs = FactoryUtil.createFilesFromNamesInDir(generatorFile.getName()
                , templateFiles, outputDir);
        Set<Map.Entry<File, File>> entrySet = templateToOutputs.entrySet();

        for (Map.Entry<File, File> fileFileEntry : entrySet) {
            File templateFile = fileFileEntry.getKey();
            File outputFile = fileFileEntry.getValue();
            if (!outputFile.exists()) {
                if (outputFile.lastModified() > generatorFile.lastModified()
                        && outputFile.lastModified() > templateFile.lastModified()
                        && outputFile.lastModified() > pomModifiedTimestamp) {
                    continue;
                }
            }
            else {
                if (outputDir.toFile().isFile()) {
                    throw new RuntimeException("Parent directory of output file is a file: "
                            + outputDir.toFile().getAbsoluteFile());
                }
            }

            Template template;
            try {
                template = config.getTemplate(templateFile.getName());
            } catch (Throwable t) {
                throw new RuntimeException("Could not read template: " + templateFile.getName(), t);
            }

            try (FileWriter writer = new FileWriter(outputFile)) {
                template.process(dataModel, writer);
            } catch (Throwable t) {
                throw new RuntimeException("Could not process template associated with data file: " + generatorLocation, t);
            }
        }
    }
}
