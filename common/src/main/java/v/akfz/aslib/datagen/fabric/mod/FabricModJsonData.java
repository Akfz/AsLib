package v.akfz.aslib.datagen.fabric.mod;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.datagen.api.Serializable;
import v.akfz.aslib.datagen.forge.modstoml.ModsTomlData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class FabricModJsonData implements Serializable<String> {

    private int schemaVersion = 1;
    private String id;
    private String version;
    private String name;
    private String description = "";
    private String license;
    private String icon;
    private String environment = "*"; // "*", "client", "server"

    private final List<String> authors = new ArrayList<>();
    private final List<String> contributors = new ArrayList<>();
    private final Map<String, String> contact = new LinkedHashMap<>();

    private final Map<String, List<String>> entrypoints = new LinkedHashMap<>();
    private final List<String> mixins = new ArrayList<>();

    private final Map<String, String> depends = new LinkedHashMap<>();
    private final Map<String, String> recommends = new LinkedHashMap<>();
    private final Map<String, String> suggests = new LinkedHashMap<>();
    private final Map<String, String> conflicts = new LinkedHashMap<>();
    private final Map<String, String> breaks = new LinkedHashMap<>();

    private final Map<String, Object> custom = new LinkedHashMap<>();

    public FabricModJsonData() {
        Properties props = loadGradleProperties();

        this.id = props.getProperty("mod_id", "aslib");
        this.version = props.getProperty("mod_version", "1.0.0");
        this.name = props.getProperty("mod_name", "Unnamed Mod");
        this.description = props.getProperty("mod_description", "");
        this.license = props.getProperty("mod_license", "MIT");
        this.icon = props.getProperty("mod_icon", "assets/aslib/icon.png");

        String loaderVer = props.getProperty("fabric_loader_version", "0.15.11");
        String mcVer = props.getProperty("minecraft_version", "1.20.1");

        this.depend("fabricloader", ">=" + loaderVer);
        this.depend("minecraft", mcVer);

        String authorsProp = props.getProperty("mod_authors", "Akaize");
        for (String a : authorsProp.split(",\\s*")) {
            if (!a.isEmpty()) this.authors.add(a);
        }

        String homepage = props.getProperty("mod_homepage", "");
        if (!homepage.isEmpty()) this.contact.put("homepage", homepage);

        String issues = props.getProperty("mod_issues", "");
        if (!issues.isEmpty()) this.contact.put("issues", issues);

        String mixinProp = this.id + ".mixins.json";
        this.mixins.add(mixinProp);
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

        if (forgeData.getDisplayURL() != null && !forgeData.getDisplayURL().isEmpty()) {
            fabricData.contact("homepage", forgeData.getDisplayURL());
        }
        if (forgeData.getIssueTrackerURL() != null && !forgeData.getIssueTrackerURL().isEmpty()) {
            fabricData.contact("issues", forgeData.getIssueTrackerURL());
        }

        if (forgeData.getAuthors() != null) {
            fabricData.authors.clear();
            for (String a : forgeData.getAuthors().split(",\\s*")) {
                if (!a.isEmpty()) fabricData.authors.add(a);
            }
        }

        if (!forgeData.getMixins().isEmpty()) {
            fabricData.mixins.clear();
            fabricData.mixins.addAll(forgeData.getMixins());
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

    public FabricModJsonData id(String id) { this.id = id; return this; }
    public FabricModJsonData version(String version) { this.version = version; return this; }
    public FabricModJsonData name(String name) { this.name = name; return this; }
    public FabricModJsonData description(String desc) { this.description = desc; return this; }
    public FabricModJsonData license(String license) { this.license = license; return this; }
    public FabricModJsonData icon(String icon) { this.icon = icon; return this; }
    public FabricModJsonData environment(String env) { this.environment = env; return this; }

    public FabricModJsonData author(String author) { this.authors.add(author); return this; }
    public FabricModJsonData contributor(String contributor) { this.contributors.add(contributor); return this; }
    public FabricModJsonData contact(String key, String value) { this.contact.put(key, value); return this; }

    public FabricModJsonData entrypoint(String category, String className) {
        this.entrypoints.computeIfAbsent(category, k -> new ArrayList<>()).add(className);
        return this;
    }

    public FabricModJsonData entrypoint(String className) {
        return entrypoint("main", className);
    }

    public FabricModJsonData mixin(String mixin) {
        if (!this.mixins.contains(mixin)) {
            this.mixins.add(mixin);
        }
        return this;
    }

    public FabricModJsonData depend(String modId, String versionRange) { this.depends.put(modId, versionRange); return this; }
    public FabricModJsonData recommend(String modId, String versionRange) { this.recommends.put(modId, versionRange); return this; }
    public FabricModJsonData suggest(String modId, String versionRange) { this.suggests.put(modId, versionRange); return this; }
    public FabricModJsonData conflict(String modId, String versionRange) { this.conflicts.put(modId, versionRange); return this; }
    public FabricModJsonData breakMod(String modId, String versionRange) { this.breaks.put(modId, versionRange); return this; }

    public FabricModJsonData custom(String key, Object value) { this.custom.put(key, value); return this; }

    public String getId() { return id; }
    public String getVersion() { return version; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLicense() { return license; }
    public String getIcon() { return icon; }
    public Map<String, String> getContact() { return Collections.unmodifiableMap(contact); }
    public List<String> getMixins() { return Collections.unmodifiableList(mixins); }
    public Map<String, String> getDepends() { return Collections.unmodifiableMap(depends); }

    @Override public @Nullable ResourceLocation getRLPath() { return null; }
    @Override public Path getPath() { return Path.of("fabric.mod.json"); }
    @Override public boolean isSystem() { return true; }
    @Override public String getExtension() { return "json"; }

    @Override
    public String serialize() {
        StringBuilder json = new StringBuilder("{\n");
        json.append(String.format("  \"schemaVersion\": %d,\n", schemaVersion));
        json.append(String.format("  \"id\": \"%s\",\n", id));
        json.append(String.format("  \"version\": \"%s\",\n", version));
        json.append(String.format("  \"name\": \"%s\",\n", name));

        String finalDesc = (description != null && !description.isEmpty()) ? description : "Mod created with ASLib";
        json.append(String.format("  \"description\": \"%s\",\n", finalDesc.replace("\"", "\\\"").replace("\n", "\\n")));

        json.append(String.format("  \"license\": \"%s\",\n", license));
        json.append(String.format("  \"icon\": \"%s\",\n", icon));
        json.append(String.format("  \"environment\": \"%s\",\n", environment));

        json.append("  \"authors\": [\n");
        for (int i = 0; i < authors.size(); i++) {
            json.append(String.format("    \"%s\"", authors.get(i)));
            if (i < authors.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");

        if (!contributors.isEmpty()) {
            json.append("  \"contributors\": [\n");
            for (int i = 0; i < contributors.size(); i++) {
                json.append(String.format("    \"%s\"", contributors.get(i)));
                if (i < contributors.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  ],\n");
        }

        if (!contact.isEmpty()) {
            json.append("  \"contact\": {\n");
            int i = 0;
            for (Map.Entry<String, String> entry : contact.entrySet()) {
                json.append(String.format("    \"%s\": \"%s\"", entry.getKey(), entry.getValue()));
                if (i++ < contact.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  },\n");
        }

        json.append("  \"entrypoints\": {\n");
        int epCategoryIndex = 0;
        for (Map.Entry<String, List<String>> entry : entrypoints.entrySet()) {
            json.append(String.format("    \"%s\": [\n", entry.getKey()));
            List<String> classes = entry.getValue();
            for (int j = 0; j < classes.size(); j++) {
                json.append(String.format("      \"%s\"", classes.get(j)));
                if (j < classes.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("    ]");
            if (epCategoryIndex++ < entrypoints.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  },\n");

        json.append("  \"mixins\": [\n");
        for (int i = 0; i < mixins.size(); i++) {
            json.append(String.format("    \"%s\"", mixins.get(i)));
            if (i < mixins.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  ],\n");

        appendDependencyMap(json, "depends", depends, true);

        if (!recommends.isEmpty()) appendDependencyMap(json, "recommends", recommends, true);
        if (!suggests.isEmpty()) appendDependencyMap(json, "suggests", suggests, true);
        if (!conflicts.isEmpty()) appendDependencyMap(json, "conflicts", conflicts, true);
        if (!breaks.isEmpty()) appendDependencyMap(json, "breaks", breaks, true);

        if (!custom.isEmpty()) {
            json.append("  \"custom\": {\n");
            int i = 0;
            for (Map.Entry<String, Object> entry : custom.entrySet()) {
                json.append(String.format("    \"%s\": \"%s\"", entry.getKey(), entry.getValue().toString().replace("\"", "\\\"")));
                if (i++ < custom.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("  }\n");
        } else {
            if (json.charAt(json.length() - 2) == ',') {
                json.deleteCharAt(json.length() - 2);
            }
        }

        json.append("}");
        return json.toString();
    }

    private void appendDependencyMap(StringBuilder json, String name, Map<String, String> map, boolean trailingComma) {
        json.append(String.format("  \"%s\": {\n", name));
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            json.append(String.format("    \"%s\": \"%s\"", entry.getKey(), entry.getValue()));
            if (i++ < map.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("  }");
        if (trailingComma) json.append(",");
        json.append("\n");
    }
}