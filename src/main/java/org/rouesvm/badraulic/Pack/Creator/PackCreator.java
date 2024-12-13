package org.rouesvm.badraulic.Pack.Creator;

import org.rouesvm.badraulic.Pack.Reader.PackReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PackCreator {
    public static final String PATH_NAME = "geyser_jsons";

    public static void createBasePack() throws IOException {
        Path pack = Files.createDirectories(Path.of(PATH_NAME + "/pack"));

        Path texturePath = Path.of(pack + "/textures");
        Path textures = Files.createDirectories(texturePath);

        createModFiles(textures);

        List<Path> modTextures = new ArrayList<>();
        String inputZip = "polymer/resource_pack_unzipped";
        String[] modTextureNames = {"textures/item", "textures/block"};
        Arrays.stream(modTextureNames).iterator().forEachRemaining(textureName -> {
            try {
                modTextures.addAll(findPngFiles(inputZip, textureName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println(modTextures);
    }

    private static void createModFiles(Path textures) throws IOException {
        List<Path> mappings = getTextures();
        List<String> modNames = new ArrayList<>();

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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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
