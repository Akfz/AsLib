package n.paradox.aslib.datagen.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.util.Identifier;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// базовый правайдер для генерации DataSerializable
public abstract class DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final List<DataSerializable> dataList = new ArrayList<>();

    protected abstract void registerDataSerializable();

    protected final void add(DataSerializable data) {
        if (data == null) {
            throw new IllegalArgumentException("DataSerializable cannot be null!");
        }
        this.dataList.add(data);
    }

    // запуск, вызывать единожды (если не было ошибок)
    public void run() {
        registerDataSerializable();

        for (DataSerializable dataSerializable : dataList) {
            Identifier identifier = dataSerializable.getPath();
            Path rootPath = Path.of(System.getProperty("user.dir"), "src", "generated", "resources");
            Path filePath = rootPath
                    .resolve("assets")
                    .resolve(identifier.getNamespace())
                    .resolve(identifier.getPath() + ".json");

            try {
                Files.createDirectories(filePath.getParent());

                JsonElement jsonElement = dataSerializable.serialize();
                if (jsonElement == null) {
                    System.out.println("jsonElement from : " + identifier + " is null");
                    continue;
                }
                try (FileWriter writer = new FileWriter(filePath.toFile())) {
                    GSON.toJson(jsonElement,writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
