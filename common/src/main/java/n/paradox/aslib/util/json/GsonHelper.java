package n.paradox.aslib.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GsonHelper {
    public static final Gson DEFAULT_GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    public static <T extends JsonData> void write(JsonFile<T> file) {
        write(file, DEFAULT_GSON);
    }

    public static <T extends JsonData> void write(JsonFile<T> file, Gson gson) {
        Path path = file.getPath();

        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                gson.toJson(file.data(), writer);
            }
        } catch (IOException e) {
            logError("[AsLib] Critical error writing JSON: " + path.getFileName() + " -> " + e.getMessage());
        }
    }

    @Nullable
    public static <T extends JsonData> T read(Path path, Class<T> clazz) {
        return read(path, clazz, DEFAULT_GSON);
    }

    @Nullable
    public static <T extends JsonData> T read(Path path, Class<T> clazz, Gson gson) {
        if (Files.notExists(path)) {
            logError("[AsLib] File does not exist: " + path);
            return null;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, clazz);
        } catch (Exception e) {
            logError("[AsLib] Error reading JSON from " + path + " -> " + e.getMessage());
            return null;
        }
    }

    private static void logError(String error) {
        System.err.println(error);
    }
}