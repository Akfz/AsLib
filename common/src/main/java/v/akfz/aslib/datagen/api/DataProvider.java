package v.akfz.aslib.datagen.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// базовый правайдер для генерации DataSerializable
public abstract class DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final List<Serializable<?>> dataList = new ArrayList<>();

    protected abstract void registerDataSerializable();

    protected final void add(Serializable<?> data) {
        if (data == null) {
            throw new IllegalArgumentException("DataSerializable cannot be null!");
        }
        this.dataList.add(data);
    }

    private Path resolveSubprojectRoot(String subprojectName) {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();

        Path rootProject = current;
        while (current != null) {
            if (Files.exists(current.resolve("settings.gradle")) || Files.exists(current.resolve("gradle.properties"))) {
                rootProject = current;
                break;
            }
            current = current.getParent();
        }

        String cleanedName = subprojectName;
        if (cleanedName.startsWith(":")) {
            cleanedName = cleanedName.substring(1);
        }

        String pathString = cleanedName.replace(":", "/").replace("\\", "/");

        Path subprojectPath = rootProject;
        for (String part : pathString.split("/")) {
            if (!part.isEmpty()) {
                subprojectPath = subprojectPath.resolve(part);
            }
        }

        return subprojectPath;
    }

    // запуск, вызывать единожды (если не было ошибок)
    //НЕ ЗАПУСКАТЬ В МАЙНКРАФТЕ, ТОЛЬКО ДЛЯ DEV!
    public void runToMain(String subProjectName) {
        runInternal(subProjectName, Path.of("src", "main", "resources"));
    }

    // Генерация в src/generated/resources
    public void run(String subProjectName) {
        runInternal(subProjectName, Path.of("src", "generated", "resources"));
    }

    private void runInternal(String subProjectName, Path relativeResourcesPath) {
        registerDataSerializable();

        Path subprojectRoot = resolveSubprojectRoot(subProjectName);
        Path rootPath = subprojectRoot.resolve(relativeResourcesPath);

        System.out.println("Generating resources in: " + rootPath.toAbsolutePath());

        for (Serializable<?> serializable : dataList) {
            Path filePath;

            if (serializable.isSystem()) {
                Path customPath = serializable.getPath();
                if (customPath == null) continue;

                String pathStr = customPath.toString();
                if (!pathStr.endsWith("." + serializable.getExtension())) {
                    pathStr += "." + serializable.getExtension();
                }
                filePath = rootPath.resolve(pathStr);
            } else {
                ResourceLocation rl = serializable.getRLPath();

                if (rl != null) {
                    String folderType = serializable.isAsset() ? "assets" : "data";
                    filePath = rootPath
                            .resolve(folderType)
                            .resolve(rl.getNamespace())
                            .resolve(rl.getPath() + "." + serializable.getExtension());
                } else {
                    Path fallbackPath = serializable.getPath();
                    if (fallbackPath == null) continue;
                    String pathStr = fallbackPath.toString();
                    if (!pathStr.endsWith("." + serializable.getExtension())) {
                        pathStr += "." + serializable.getExtension();
                    }
                    filePath = rootPath.resolve(pathStr);
                }
            }

            try {
                Files.createDirectories(filePath.getParent());
                Object output = serializable.serialize();

                if (output == null) continue;

                try (FileWriter writer = new FileWriter(filePath.toFile())) {
                    if (output instanceof JsonElement jsonElement) {
                        GSON.toJson(jsonElement, writer);
                    } else {
                        writer.write(output.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}