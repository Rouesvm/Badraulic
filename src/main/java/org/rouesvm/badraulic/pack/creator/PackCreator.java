package org.rouesvm.badraulic.pack.creator;

import org.jetbrains.annotations.NotNull;
import org.rouesvm.badraulic.mappings.block.BlockMappings;
import org.rouesvm.badraulic.mappings.item.ItemJsonConvertor;
import org.rouesvm.badraulic.pack.reader.PackReader;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackCreator {
    public static final String PATH_NAME = "geyser_jsons";

    public static void createBasePack() throws IOException {
        Path pack = Files.createDirectories(Path.of(PATH_NAME + "/pack"));

        Path texturePath = Path.of(pack + "/textures");
        Path textures = Files.createDirectories(texturePath);

        List<Path> modPaths = createModFiles(textures);
        Map<String, Path> modPathMap = modPaths.stream()
                .collect(Collectors.toMap(
                        modPath -> modPath.toString().replace("geyser_jsons/pack/textures/", ""),
                        modPath -> modPath
                ));

        copyBlockTexturesToPack(modPathMap);
        copyItemTexturesToPack(modPathMap);

        Path blockTexturesPath = Path.of(textures + "/" + BlockMappings.texture_json.getFileName());
        Path itemTexturesPath = Path.of(textures + "/" + ItemJsonConvertor.texture_json.getFileName());

        Files.copy(BlockMappings.texture_json, blockTexturesPath, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(ItemJsonConvertor.texture_json, itemTexturesPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void copyItemTexturesToPack(Map<String, Path> modPathMap) throws IOException {
        List<Path> modTextures = findPngFiles("polymer/resource_pack_unzipped", "textures/item");

        modTextures.parallelStream().forEach(path -> {
            String relativePath = modPathMap.keySet().stream()
                    .filter(path.toString()::contains)
                    .findFirst()
                    .orElse(null);

            if (relativePath != null) {
                try {
                    Path modDir = modPathMap.get(relativePath).resolve("item");


                    Path textureParentDir = path.getParent().getFileName();
                    if (!textureParentDir.toString().equals("item")) {
                        modDir = modDir.resolve(textureParentDir.toString());
                    }

                    Files.createDirectories(modDir);

                    Path targetPath = modDir.resolve(path.getFileName());
                    Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static void copyBlockTexturesToPack(Map<String, Path> modPathMap) throws IOException {
        List<Path> modTextures = findPngFiles("polymer/resource_pack_unzipped", "textures/block");

        modTextures.parallelStream().forEach(path -> {
            String relativePath = modPathMap.keySet().stream()
                    .filter(path.toString()::contains)
                    .findFirst()
                    .orElse(null);

            if (relativePath != null) {
                try {
                    Path modDir = modPathMap.get(relativePath).resolve("block");

                    Path fullPath = getFullPath(path, modDir);
                    Files.createDirectories(fullPath.getParent());
                    
                    Files.copy(path, fullPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static @NotNull Path getFullPath(Path path, Path modDir) {
        Path currentDir = path.getParent();
        Deque<String> dirStack = new ArrayDeque<>();

        while (currentDir != null && !currentDir.getFileName().toString().contains("block")) {
            dirStack.push(currentDir.getFileName().toString());
            currentDir = currentDir.getParent();
        }

        Path fullPath = modDir;
        while (!dirStack.isEmpty()) {
            fullPath = fullPath.resolve(dirStack.pop());
        }

        fullPath = fullPath.resolve(path.getFileName().toString());
        return fullPath;
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
