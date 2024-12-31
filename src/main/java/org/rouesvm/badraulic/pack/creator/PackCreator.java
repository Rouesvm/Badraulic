package org.rouesvm.badraulic.pack.creator;

import org.rouesvm.badraulic.pack.reader.PackReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PackCreator {
    public static final String PATH_NAME = "geyser_jsons";

    public static void createBasePack() throws IOException {
        Path pack = Files.createDirectories(Path.of(PATH_NAME + "/pack"));

        Path texturePath = Path.of(pack + "/textures");
        Path textures = Files.createDirectories(texturePath);

        List<Path> modPaths = createModFiles(textures);
    }

    private static List<Path> createModFiles(Path textures) throws IOException {
        List<Path> mappings = getTextures();
        List<String> modNames = new ArrayList<>();
        List<Path> modPaths = new ArrayList<>();

        mappings.forEach(path -> {
            String fileName = path.getFileName().toString();
            fileName = fileName.replace(".json", "")
                    .replace("_item_mappings", "");
            modNames.add(fileName);
        });

        modNames.forEach(names -> {
            try {
                Path modPath = Files.createDirectories(Path.of(textures + "/" + names));
                Files.createDirectories(Path.of(modPath + "/" + "item"));
                Files.createDirectories(Path.of(modPath + "/" + "block"));
                modPaths.add(modPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return modPaths;
    }

    public static List<Path> findPngFiles(String rootDir, String targetSubfolder) throws IOException {
        List<Path> jsonFiles = new ArrayList<>();
        Path startPath = Path.of(rootDir);

        Stream<Path> files = Files.walk(startPath);
        try (files) {
            files.filter(path -> {
                Path relativePath = startPath.relativize(path);
                return relativePath.toString().contains(targetSubfolder) &&
                        path.toString().endsWith(".png");
            }).forEach(jsonFiles::add);
        }

        return jsonFiles;
    }

    public static List<Path> getTextures() throws IOException {
        return PackReader.findJsonFiles(PATH_NAME + "/item");
    }
}
