package fr.xephi.authmebungee.common.utils;

import fr.xephi.authmebungee.common.annotations.DataFolder;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class UUIDFileGenerator {

    private final File dataFolder;

    @Inject
    UUIDFileGenerator(@DataFolder File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public UUID generateInstanceUUID() throws IOException {
        File dir = new File(String.valueOf(dataFolder),"UUID");
        if (!dir.exists()) {
            dir.mkdir();
        }
        String[] files = dir.list();
        if(files.length > 0) return UUID.fromString(files[0].replaceFirst("\\.txt",""));

        UUID uuid = UUID.randomUUID();

        Path newFile = Paths.get(dir.toString(), uuid.toString() + ".txt");
        FileUtils.create(newFile.toFile());

        return uuid;
    }
}
