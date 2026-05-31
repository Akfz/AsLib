package n.paradox.aslib.datagen.fabric.mod;

import n.paradox.aslib.datagen.api.Serializable;
import n.paradox.aslib.datagen.forge.modstoml.ModsTomlData;
import org.jetbrains.annotations.Nullable;
import net.minecraft.resources.ResourceLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class FabricModJsonData implements Serializable<String> {

    private String id;
    private String version;
    private String name;
    private String description = "";
    private String license;
    private String icon;
    private String mainEntrypoint;
    private String mixinConfig;
    private String loaderVersion;
    private String minecraftVersion;

    public FabricModJsonData() {
        Properties props = loadGradleProperties();

        this.id = props.getProperty("mod_id", "aslib");
        this.version = props.getProperty("mod_version", "1.0.0");
        this.name = props.getProperty("mod_name", "Unnamed Mod");
        this.description = props.getProperty("mod_description", "");
        this.license = props.getProperty("mod_license", "MIT");
        this.icon = props.getProperty("mod_icon", "assets/aslib/icon.png");
        this.loaderVersion = props.getProperty("fabric_loader_version", "0.15.11");
        this.minecraftVersion = props.getProperty("minecraft_version", "1.20.1");
    }

    public static FabricModJsonData fromForge(ModsTomlData forgeData) {
        FabricModJsonData fabricData = new FabricModJsonData();
        fabricData.id(forgeData.getModId())
                .version(forgeData.getVersion())
                .name(forgeData.getDisplayName())
                .description(forgeData.getDescription())
                .license(forgeData.getLicense());

        if (forgeData.getLogoFile() != null) {
            fabricData.icon("assets/" + forgeData.getModId() + "/" + forgeData.getLogoFile());
        }
        return fabricData;
    }

    private Properties loadGradleProperties() {
        Properties props = new Properties();
        File currentDir = new File(System.getProperty("user.dir"));

        File propsFile = null;
        while (currentDir != null) {
            File checkFile = new File(currentDir, "gradle.properties");
            if (checkFile.exists()) {
                propsFile = checkFile;
                break;
            }
            currentDir = currentDir.getParentFile();
        }

        if (propsFile != null && propsFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propsFile)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Ошибка чтения свойств Fabric: " + e.getMessage());
            }
        }
        return props;
    }

    public String getId() { return id; }
    public String getVersion() { return version; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLicense() { return license; }
    public String getIcon() { return icon; }
    public String getMainEntrypoint() { return mainEntrypoint; }
    public String getMixinConfig() { return mixinConfig; }

    public FabricModJsonData id(String id) { this.id = id; return this; }
    public FabricModJsonData version(String version) { this.version = version; return this; }
    public FabricModJsonData name(String name) { this.name = name; return this; }
    public FabricModJsonData description(String desc) { this.description = desc; return this; }
    public FabricModJsonData license(String license) { this.license = license; return this; }
    public FabricModJsonData icon(String icon) { this.icon = icon; return this; }
    public FabricModJsonData entrypoint(String entry) { this.mainEntrypoint = entry; return this; }
    public FabricModJsonData mixin(String mixin) { this.mixinConfig = mixin; return this; }
    public FabricModJsonData loaderVersion(String ver) { this.loaderVersion = ver; return this; }
    public FabricModJsonData mcVersion(String ver) { this.minecraftVersion = ver; return this; }

    @Override
    public String serialize() {
        StringBuilder json = new StringBuilder("{\n");
        json.append("  \"schemaVersion\": 1,\n");
        json.append(String.format("  \"id\": \"%s\",\n", id));
        json.append(String.format("  \"version\": \"%s\",\n", version));
        json.append(String.format("  \"name\": \"%s\",\n", name));

        String finalDesc = (description != null && !description.isEmpty()) ? description : "Library for structural resource packs";
        json.append(String.format("  \"description\": \"%s\",\n", finalDesc.replace("\"", "\\\"").replace("\n", "\\n")));

        json.append(String.format("  \"license\": \"%s\",\n", license));
        json.append(String.format("  \"icon\": \"%s\",\n", icon));
        json.append("  \"environment\": \"*\",\n");

        json.append("  \"authors\": [\n");
        Properties props = loadGradleProperties();
        String authorsProp = props.getProperty("mod_authors", "Akaize, SweetB1ans");
        String[] authorsArray = authorsProp.split(",\\s*");
        for (int i = 0; i < authorsArray.length; i++) {
            json.append(String.format("    \"%s\"", authorsArray[i]));
            if (i < authorsArray.length - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");

        String homepage = props.getProperty("mod_homepage", "");
        String issues = props.getProperty("mod_issues", "");
        json.append("  \"contact\": {\n");
        json.append(String.format("    \"homepage\": \"%s\",\n", homepage));
        json.append(String.format("    \"sources\": \"%s\"\n", homepage)); // Обычно совпадает с репозиторием
        json.append("  },\n");

        if (mainEntrypoint != null && !mainEntrypoint.isEmpty()) {
            json.append(String.format("  \"entrypoints\": { \"main\": [ \"%s\" ] },\n", mainEntrypoint));
        } else {
            json.append("  \"entrypoints\": {},\n");
        }

        if (mixinConfig != null && !mixinConfig.isEmpty()) {
            json.append(String.format("  \"mixins\": [ \"%s\" ],\n", mixinConfig));
        } else {
            json.append("  \"mixins\": [],\n");
        }

        json.append("  \"depends\": {\n");
        json.append(String.format("    \"fabricloader\": \">=%s\",\n", loaderVersion));
        json.append(String.format("    \"minecraft\": \"%s\"\n", minecraftVersion));
        json.append("  }\n");
        json.append("}");
        return json.toString();
    }

    @Override public @Nullable ResourceLocation getRLPath() { return null; }
    @Override public Path getPath() { return Path.of("fabric.mod.json"); }
    @Override public boolean isSystem() { return true; }
    @Override public String getExtension() { return "json"; }
}