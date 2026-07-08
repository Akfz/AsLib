package v.akfz.aslib.datagen.forge.packmcmeta;

import v.akfz.aslib.datagen.api.Serializable;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class PackMcmetaData implements Serializable<String> {

    private String description;
    private int packFormat;

    public PackMcmetaData() {
        Properties props = loadGradleProperties();

        String modName = props.getProperty("mod_name", "aslib");
        this.description = modName + " resources";

        this.packFormat = 15;
    }

    private Properties loadGradleProperties() {
        Properties props = new Properties();
        File currentDir = new File(System.getProperty("user.dir"));
        File propsFile = new File(currentDir, "gradle.properties");

        if (!propsFile.exists() && currentDir.getParentFile() != null) {
            propsFile = new File(currentDir.getParentFile(), "gradle.properties");
        }

        if (propsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propsFile)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Ошибка при чтении gradle.properties для pack.mcmeta: " + e.getMessage());
            }
        }
        return props;
    }

    public PackMcmetaData description(String description) {
        this.description = description;
        return this;
    }

    public PackMcmetaData packFormat(int packFormat) {
        this.packFormat = packFormat;
        return this;
    }

    @Override
    public String serialize() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"pack\": {\n");
        json.append(String.format("    \"description\": \"%s\",\n", description));
        json.append(String.format("    \"pack_format\": %d\n", packFormat));
        json.append("  }\n");
        json.append("}");
        return json.toString();
    }

    @Override
    public @Nullable ResourceLocation getRLPath() {
        return null;
    }

    @Override
    public Path getPath() {
        return Path.of("pack");
    }

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public String getExtension() {
        return "mcmeta";
    }
}