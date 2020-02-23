package com.marklogic.utils;

import org.gradle.internal.impldep.org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;


public class BaseTest {

    private File projectDir = Paths.get("").toAbsolutePath().toFile();

    public final TaskOutcome SUCCESS = TaskOutcome.SUCCESS;
    public final TaskOutcome FAILED = TaskOutcome.FAILED;
    public final Path MLCONFIG = projectDir.toPath().resolve("src").resolve("main").resolve("ml-config");
    public final Path MLMODULES = projectDir.toPath().resolve("src").resolve("main").resolve("ml-modules");
    public final Path MLSCHEMAS = projectDir.toPath().resolve("src").resolve("main").resolve("ml-schemas");



    private Properties loadProperties() {
        try {
            File gradleProperties = new File(projectDir, "gradle.properties");
            Properties props = new Properties();
            FileInputStream propsStream = new FileInputStream(gradleProperties);
            props.load(propsStream);
            return props;
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected BuildResult runTask(String... task) {
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(task)
                .withDebug(true)
                .build();
    }

    protected BuildResult runFailedTask(String... task) {
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(task)
                .withDebug(true)
                .buildAndFail();
    }

    protected static File getResourceFile(String resourceName) {
        return new File(BaseTest.class.getClassLoader().getResource(resourceName).getFile());
    }

    protected void copyResourcesIntoProject(String resourceName, String projectPath) {
        try {
            FileUtils.copyFileToDirectory(
                    getResourceFile(resourceName),
                    projectDir.toPath().resolve(projectPath).toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void copyDirectoryIntoProject(String resourceName, String projectPath) {
        try {
            FileUtils.copyDirectoryToDirectory(
                    getResourceFile(resourceName),
                    projectDir.toPath().resolve(projectPath).toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void copyResourcesIntoConfigDir(String resourceName, String configDirpath) {
        try {
            FileUtils.copyFileToDirectory(
                    getResourceFile(resourceName),
                    MLCONFIG.resolve(configDirpath).toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void copyDirectoriesIntoConfigDir(String resourceName, String configDirPath) {

        try {
            FileUtils.copyDirectoryToDirectory(
                    getResourceFile(resourceName),
                    MLCONFIG.resolve(configDirPath).toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean removeDirectoriesFromProject(String configDirPath) {
        if(configDirPath!="src/main")
        return FileUtils.deleteQuietly(MLCONFIG.resolve(configDirPath).toFile());
        else return FileUtils.deleteQuietly(MLMODULES.toFile());
    }

    protected void loadModules() {
        runTask(":mlLoadModules", "-PmlUsername=dh-developer", "-PmlPassword=dh-developer");
    }

}
