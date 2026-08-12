package v.akfz.aslib.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.util.GlobalUtils;

import java.io.*;
import java.nio.charset.Charset;
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

    @Nullable
    public static <T extends JsonData> T read(ResourceLocation location, Class<T> clazz) {
        if (!GlobalUtils.isClientHost()) return null;
        return DEFAULT_GSON.fromJson(getFileContents(location, Minecraft.getInstance().getResourceManager()),clazz);
    }

    @Nullable
    public static <T extends JsonData> T read(ResourceLocation location, Class<T> clazz, ResourceManager manager) {
        return DEFAULT_GSON.fromJson(getFileContents(location, manager),clazz);
    }

    @Nullable
    public static <T extends JsonData> T read(ResourceLocation location, Class<T> clazz, Gson gson) {
        if (!GlobalUtils.isClientHost()) return null;
        return gson.fromJson(getFileContents(location, Minecraft.getInstance().getResourceManager()),clazz);
    }

    @Nullable
    public static <T extends JsonData> T read(ResourceLocation location, Class<T> clazz, ResourceManager manager, Gson gson) {
        return gson.fromJson(getFileContents(location, manager),clazz);
    }

    @Nullable
    public static String getFileContents(ResourceLocation location, ResourceManager manager) {
        try (InputStream inputStream = manager.getResourceOrThrow(location).open()) {
            return IOUtils.toString(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
            logError("[AsLib] : " + e.toString());
        }
        return null;
    }

    private static void logError(String error) {
        System.err.println(error);
    }
}