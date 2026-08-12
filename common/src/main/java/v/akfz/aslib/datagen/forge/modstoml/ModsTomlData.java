package v.akfz.aslib.datagen.forge.modstoml;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.datagen.api.Serializable;
import v.akfz.aslib.datagen.fabric.mod.FabricModJsonData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ModsTomlData implements Serializable<String> {

    private String modLoader = "javafml";
    private String loaderVersion = "[47,)";
    private String license;
    private String issueTrackerURL;

    private String modId;
    private String version;
    private String displayName;
    private String logoFile;
    private String authors;
    private String credits;
    private String description;
    private String displayURL;

    private final List<String> mixins = new ArrayList<>();
    private final List<Dependency> dependencies = new ArrayList<>();

    public ModsTomlData() {
        Properties props = loadGradleProperties();

        this.modId = props.getProperty("mod_id", "aslib");
        this.version = props.getProperty("mod_version", "1.0.0");
        this.displayName = props.getProperty("mod_name", "Unnamed Mod");
        this.authors = props.getProperty("mod_authors", "Akaize");
        this.description = props.getProperty("mod_description", "");
        this.license = props.getProperty("mod_license", "MIT");
        this.displayURL = props.getProperty("mod_homepage", "");
        this.issueTrackerURL = props.getProperty("mod_issues", "");

        String fullIconPath = props.getProperty("mod_icon", "icon.png");
        this.logoFile = fullIconPath.contains("/") ? fullIconPath.substring(fullIconPath.lastIndexOf("/") + 1) : fullIconPath;

        String mcVer = props.getProperty("minecraft_version", "1.20.1");

        this.dependency("minecraft", true, "[" + mcVer + ",1.21)", "NONE", "BOTH");
        this.dependency("forge", true, "[47,)", "NONE", "BOTH");

        String mixinProp = this.modId + ".mixins.json";
        this.mixins.add(mixinProp);
    }

    public static ModsTomlData fromFabric(FabricModJsonData fabricData) {
        ModsTomlData forgeData = new ModsTomlData();
        forgeData.modId(fabricData.getId())
                .version(fabricData.getVersion())
                .displayName(fabricData.getName())
                .description(fabricData.getDescription())
                .license(fabricData.getLicense());

        if (fabricData.getIcon() != null) {
            String iconPath = fabricData.getIcon();
            forgeData.logoFile(iconPath.contains("/") ? iconPath.substring(iconPath.lastIndexOf("/") + 1) : iconPath);
        }

        if (fabricData.getContact().containsKey("homepage")) {
            forgeData.displayURL(fabricData.getContact().get("homepage"));
        }
        if (fabricData.getContact().containsKey("issues")) {
            forgeData.issueTrackerURL(fabricData.getContact().get("issues"));
        }

        if (!fabricData.getMixins().isEmpty()) {
            forgeData.mixins.clear();
            forgeData.mixins.addAll(fabricData.getMixins());
        }

        return forgeData;
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
                System.err.println("Ошибка чтения gradle.properties: " + e.getMessage());
            }
        }
        return props;
    }

    public ModsTomlData modLoader(String modLoader) { this.modLoader = modLoader; return this; }
    public ModsTomlData loaderVersion(String loaderVersion) { this.loaderVersion = loaderVersion; return this; }
    public ModsTomlData license(String license) { this.license = license; return this; }
    public ModsTomlData issueTrackerURL(String url) { this.issueTrackerURL = url; return this; }
    public ModsTomlData modId(String modId) { this.modId = modId; return this; }
    public ModsTomlData version(String version) { this.version = version; return this; }
    public ModsTomlData displayName(String name) { this.displayName = name; return this; }
    public ModsTomlData logoFile(String logo) { this.logoFile = logo; return this; }
    public ModsTomlData authors(String authors) { this.authors = authors; return this; }
    public ModsTomlData credits(String credits) { this.credits = credits; return this; }
    public ModsTomlData description(String desc) { this.description = desc; return this; }
    public ModsTomlData displayURL(String url) { this.displayURL = url; return this; }

    public ModsTomlData mixin(String mixinConfig) {
        if (!this.mixins.contains(mixinConfig)) {
            this.mixins.add(mixinConfig);
        }
        return this;
    }

    public ModsTomlData dependency(String modId, boolean mandatory, String versionRange, String ordering, String side) {
        this.dependencies.add(new Dependency(modId, mandatory, versionRange, ordering, side, mandatory ? "required" : "optional"));
        return this;
    }

    public ModsTomlData dependency(String modId, boolean mandatory, String versionRange, String ordering, String side, String type) {
        this.dependencies.add(new Dependency(modId, mandatory, versionRange, ordering, side, type));
        return this;
    }

    public ModsTomlData dependency(String modId, boolean mandatory, String versionRange) {
        return dependency(modId, mandatory, versionRange, "NONE", "BOTH");
    }

    public ModsTomlData clearDependencies() {
        this.dependencies.clear();
        return this;
    }

    public String getModId() { return modId; }
    public String getVersion() { return version; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getLicense() { return license; }
    public String getLogoFile() { return logoFile; }
    public String getAuthors() { return authors; }
    public String getDisplayURL() { return displayURL; }
    public String getIssueTrackerURL() { return issueTrackerURL; }
    public List<Dependency> getDependencies() { return Collections.unmodifiableList(dependencies); }
    public List<String> getMixins() { return Collections.unmodifiableList(mixins); }

    @Override public @Nullable ResourceLocation getRLPath() { return null; }
    @Override public Path getPath() { return Path.of("META-INF", "mods.toml"); }
    @Override public boolean isSystem() { return true; }
    @Override public String getExtension() { return "toml"; }

    @Override
    public String serialize() {
        StringBuilder toml = new StringBuilder();

        toml.append(String.format("modLoader=\"%s\"\n", modLoader));
        toml.append(String.format("loaderVersion=\"%s\"\n", loaderVersion));
        toml.append(String.format("license=\"%s\"\n", license));
        if (issueTrackerURL != null && !issueTrackerURL.isEmpty()) {
            toml.append(String.format("issueTrackerURL=\"%s\"\n", issueTrackerURL));
        }
        toml.append("\n");

        toml.append("[[mods]]\n");
        toml.append(String.format("modId=\"%s\"\n", modId));
        toml.append(String.format("version=\"%s\"\n", version));
        toml.append(String.format("displayName=\"%s\"\n", displayName));
        if (logoFile != null && !logoFile.isEmpty()) toml.append(String.format("logoFile=\"%s\"\n", logoFile));
        if (authors != null && !authors.isEmpty()) toml.append(String.format("authors=\"%s\"\n", authors));
        if (credits != null && !credits.isEmpty()) toml.append(String.format("credits=\"%s\"\n", credits));
        if (displayURL != null && !displayURL.isEmpty()) toml.append(String.format("displayURL=\"%s\"\n", displayURL));

        String cleanDesc = description != null ? description.replace("\"\"\"", "\\\"\\\"\\\"") : "";
        toml.append(String.format("description=\"\"\"\n%s\"\"\"\n\n", cleanDesc));

        for (String mixin : mixins) {
            toml.append("[[mixins]]\n");
            toml.append(String.format("config=\"%s\"\n\n", mixin));
        }

        for (Dependency dep : dependencies) {
            toml.append(String.format("[[dependencies.%s]]\n", this.modId));
            toml.append(String.format("    modId=\"%s\"\n", dep.modId));
            toml.append(String.format("    mandatory=%b\n", dep.mandatory));
            toml.append(String.format("    versionRange=\"%s\"\n", dep.versionRange));
            toml.append(String.format("    ordering=\"%s\"\n", dep.ordering));
            toml.append(String.format("    side=\"%s\"\n", dep.side));
            if (dep.type != null && !dep.type.isEmpty()) {
                toml.append(String.format("    type=\"%s\"\n", dep.type));
            }
            toml.append("\n");
        }

        return toml.toString();
    }

    public record Dependency(String modId, boolean mandatory, String versionRange, String ordering, String side, String type) {}
}