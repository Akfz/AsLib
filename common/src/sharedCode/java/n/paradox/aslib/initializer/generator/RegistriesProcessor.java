package n.paradox.aslib.initializer.generator;

import n.paradox.aslib.annotation.RegisterModule;
import n.paradox.aslib.registry.RegistryType;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({
        "n.paradox.aslib.initializer.generator.GenerateRegistries",
        "n.paradox.aslib.annotation.RegisterModule"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class RegistriesProcessor extends AbstractProcessor {

    private final List<String> generatedRegistrars = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Filer filer = processingEnv.getFiler();
        Messager messager = processingEnv.getMessager();

        String currentTarget = processingEnv.getOptions().get("modLoaderTarget");
        if (currentTarget == null) {
            currentTarget = "fabric";
        }

        List<RegistryFieldData> allAnnotatedFields = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(RegisterModule.class)) {
            if (element.getKind() == ElementKind.FIELD) {
                RegisterModule regAnno = element.getAnnotation(RegisterModule.class);
                if (regAnno != null) {
                    String id = regAnno.id();
                    RegistryType type = regAnno.registry();
                    String customRegistry = regAnno.customRegistry();
                    String fieldName = element.getSimpleName().toString();
                    String declaringClass = ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();

                    if (type == RegistryType.AUTO) {
                        type = determineRegistryType(element);
                    }

                    if (type == RegistryType.AUTO) {
                        messager.printMessage(Diagnostic.Kind.ERROR,
                                "Could not auto-determine registry type for field " + fieldName +
                                        " in class " + declaringClass + ". Please specify 'registry' parameter explicitly.", element);
                        continue;
                    }

                    allAnnotatedFields.add(new RegistryFieldData(declaringClass, fieldName, id, type, customRegistry));
                }
            }
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateRegistries.class)) {
            if (element instanceof TypeElement typeElement) {
                GenerateRegistries generateAnno = typeElement.getAnnotation(GenerateRegistries.class);
                String modId = generateAnno.modId();
                String packageId = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
                String className = typeElement.getSimpleName().toString();

                List<RegistryFieldData> modFields = new ArrayList<>();
                for (RegistryFieldData field : allAnnotatedFields) {
                    if (field.id.startsWith(modId) || field.id.startsWith(modId + ":")) {
                        modFields.add(field);
                    }
                }

                if (modFields.isEmpty()) {
                    continue;
                }

                boolean hasCommands = false;
                for (RegistryFieldData field : modFields) {
                    if (field.type == RegistryType.COMMAND) {
                        hasCommands = true;
                        break;
                    }
                }

                if (currentTarget.equalsIgnoreCase("fabric")) {
                    String fullLoaderName = generateLoader(filer, packageId, className, modFields, true);
                    if (fullLoaderName != null) {
                        generatedRegistrars.add(fullLoaderName);
                    }
                } else {
                    if (hasCommands) {
                        String fullLoaderName = generateLoader(filer, packageId, className, modFields, false);
                        if (fullLoaderName != null) {
                            generatedRegistrars.add(fullLoaderName);
                        }
                    }

                    if (currentTarget.equalsIgnoreCase("forge")) {
                        generateForgeRegistrar(filer, packageId, className, modId, modFields);
                    } else if (currentTarget.equalsIgnoreCase("neoforge")) {
                        generateNeoForgeRegistrar(filer, packageId, className, modId, modFields);
                    }
                }
            }
        }

        if (roundEnv.processingOver() && !generatedRegistrars.isEmpty()) {
            generateSpiFile(filer);
        }

        return true;
    }

    private RegistryType determineRegistryType(Element element) {
        TypeMirror fieldType = element.asType();
        if (fieldType == null) return RegistryType.AUTO;

        Types typeUtils = processingEnv.getTypeUtils();
        Element current = typeUtils.asElement(fieldType);

        while (current instanceof TypeElement typeElement) {
            String canonicalName = typeElement.getQualifiedName().toString();
            if (canonicalName.equals("net.minecraft.world.level.block.Block")) {
                return RegistryType.BLOCK;
            }
            if (canonicalName.equals("net.minecraft.world.item.Item")) {
                return RegistryType.ITEM;
            }
            if (canonicalName.equals("net.minecraft.sounds.SoundEvent")) {
                return RegistryType.SOUND_EVENT;
            }
            if (canonicalName.equals("net.minecraft.world.level.block.entity.BlockEntityType")) {
                return RegistryType.BLOCK_ENTITY_TYPE;
            }
            if (canonicalName.equals("net.minecraft.world.entity.EntityType")) {
                return RegistryType.ENTITY_TYPE;
            }
            if (canonicalName.equals("net.minecraft.world.level.material.Fluid")) {
                return RegistryType.FLUID;
            }
            if (canonicalName.equals("net.minecraft.world.item.CreativeModeTab")) {
                return RegistryType.CREATIVE_MODE_TAB;
            }

            for (TypeMirror iface : typeElement.getInterfaces()) {
                if (iface == null) continue;
                Element ifaceElement = typeUtils.asElement(iface);
                if (ifaceElement instanceof TypeElement ifaceType) {
                    String ifaceName = ifaceType.getQualifiedName().toString();
                    if (ifaceName.equals("net.minecraft.world.level.block.Block")) return RegistryType.BLOCK;
                    if (ifaceName.equals("net.minecraft.world.item.Item")) return RegistryType.ITEM;
                    if (ifaceName.equals("n.paradox.aslib.command.IRegCommand")) return RegistryType.COMMAND;
                }
            }

            TypeMirror superclass = typeElement.getSuperclass();
            if (superclass == null) break;
            current = typeUtils.asElement(superclass);
        }
        return RegistryType.AUTO;
    }

    private String generateLoader(Filer filer, String packageId, String className, List<RegistryFieldData> fields, boolean fabricOnly) {
        String generatedClassName = className + "_Loader";
        String fullPath = packageId + "." + generatedClassName;

        try {
            JavaFileObject fileObject = filer.createSourceFile(fullPath);
            try (Writer writer = fileObject.openWriter()) {
                writer.write("package " + packageId + ";\n\n");
                if (fabricOnly) {
                    writer.write("import net.minecraft.core.Registry;\n");
                    writer.write("import net.minecraft.core.registries.BuiltInRegistries;\n");
                    writer.write("import net.minecraft.resources.ResourceLocation;\n\n");
                }

                writer.write("public class " + generatedClassName + " implements n.paradox.aslib.initializer.generator.IRegistryLoader {\n\n");
                writer.write("    @Override\n");
                writer.write("    public void run() {\n");
                writer.write("        registerAll();\n");
                writer.write("    }\n\n");

                writer.write("    public static void registerAll() {\n");

                if (fabricOnly) {
                    java.util.Set<String> usedRegistries = new java.util.LinkedHashSet<>();
                    for (RegistryFieldData field : fields) {
                        if (field.type != RegistryType.COMMAND) {
                            if (field.type == RegistryType.CUSTOM) {
                                usedRegistries.add("custom:" + field.customRegistry);
                            } else {
                                usedRegistries.add("builtin:" + getBuiltInRegistryFieldName(field.type));
                            }
                        }
                    }

                    for (String regKey : usedRegistries) {
                        if (regKey.startsWith("builtin:")) {
                            String builtInKey = regKey.substring("builtin:".length());
                            writer.write("        unfreeze(BuiltInRegistries." + builtInKey + ");\n");
                        } else {
                            String customRegistry = regKey.substring("custom:".length());
                            String customRl = "new ResourceLocation(\"" + getNamespace(customRegistry) + "\", \"" + getPath(customRegistry) + "\")";
                            writer.write("        unfreeze((Registry<?>) BuiltInRegistries.REGISTRY.get(" + customRl + "));\n");
                        }
                    }

                    for (RegistryFieldData field : fields) {
                        if (field.type == RegistryType.COMMAND) {
                            String fieldRef = field.declaringClass + "." + field.name;
                            writer.write("        n.paradox.aslib.command.CommandHandler.addCommand(" + fieldRef + ");\n");
                        } else {
                            String rlExpr = "new ResourceLocation(\"" + getNamespace(field.id) + "\", \"" + getPath(field.id) + "\")";
                            String fieldRef = field.declaringClass + "." + field.name;

                            if (field.type == RegistryType.CUSTOM) {
                                String customRl = "new ResourceLocation(\"" + getNamespace(field.customRegistry) + "\", \"" + getPath(field.customRegistry) + "\")";
                                writer.write("        {\n");
                                writer.write("            Registry<Object> customReg = (Registry<Object>) BuiltInRegistries.REGISTRY.get(" + customRl + ");\n");
                                writer.write("            if (customReg != null) {\n");
                                writer.write("                Registry.register(customReg, " + rlExpr + ", " + fieldRef + ");\n");
                                writer.write("            }\n");
                                writer.write("        }\n");
                            } else {
                                String builtInKey = getBuiltInRegistryFieldName(field.type);
                                writer.write("        Registry.register(BuiltInRegistries." + builtInKey + ", " + rlExpr + ", " + fieldRef + ");\n");
                            }
                        }
                    }

                    for (String regKey : usedRegistries) {
                        if (regKey.startsWith("builtin:")) {
                            String builtInKey = regKey.substring("builtin:".length());
                            writer.write("        freeze(BuiltInRegistries." + builtInKey + ");\n");
                        } else {
                            String customRegistry = regKey.substring("custom:".length());
                            String customRl = "new ResourceLocation(\"" + getNamespace(customRegistry) + "\", \"" + getPath(customRegistry) + "\")";
                            writer.write("        freeze((Registry<?>) BuiltInRegistries.REGISTRY.get(" + customRl + "));\n");
                        }
                    }
                } else {
                    for (RegistryFieldData field : fields) {
                        if (field.type == RegistryType.COMMAND) {
                            String fieldRef = field.declaringClass + "." + field.name;
                            writer.write("        n.paradox.aslib.command.CommandHandler.addCommand(" + fieldRef + ");\n");
                        }
                    }
                }

                writer.write("    }\n\n");

                if (fabricOnly) {
                    writer.write("    private static void unfreeze(net.minecraft.core.Registry<?> registry) {\n");
                    writer.write("        if (registry == null) return;\n");
                    writer.write("        if (!(registry instanceof net.minecraft.core.MappedRegistry<?>)) return;\n");
                    writer.write("        net.minecraft.core.MappedRegistry<?> mapped = (net.minecraft.core.MappedRegistry<?>) registry;\n");
                    writer.write("        try {\n");
                    writer.write("            java.lang.reflect.Field frozenField = null;\n");
                    writer.write("            for (java.lang.reflect.Field f : net.minecraft.core.MappedRegistry.class.getDeclaredFields()) {\n");
                    writer.write("                if (f.getType() == boolean.class) {\n");
                    writer.write("                    frozenField = f;\n");
                    writer.write("                    break;\n");
                    writer.write("                }\n");
                    writer.write("            }\n");
                    writer.write("            if (frozenField != null) {\n");
                    writer.write("                frozenField.setAccessible(true);\n");
                    writer.write("                frozenField.set(mapped, false);\n");
                    writer.write("            }\n\n");

                    writer.write("            java.lang.reflect.Field intrusiveField = null;\n");
                    writer.write("            for (java.lang.reflect.Field f : net.minecraft.core.MappedRegistry.class.getDeclaredFields()) {\n");
                    writer.write("                if (java.util.Map.class.isAssignableFrom(f.getType())) {\n");
                    writer.write("                    f.setAccessible(true);\n");
                    writer.write("                    if (f.get(mapped) == null) {\n");
                    writer.write("                        intrusiveField = f;\n");
                    writer.write("                        break;\n");
                    writer.write("                    }\n");
                    writer.write("                }\n");
                    writer.write("            }\n");
                    writer.write("            if (intrusiveField == null) {\n");
                    writer.write("                for (String name : new String[]{\"unregisteredIntrusiveHolders\", \"m\"}) {\n");
                    writer.write("                    try {\n");
                    writer.write("                        intrusiveField = net.minecraft.core.MappedRegistry.class.getDeclaredField(name);\n");
                    writer.write("                        break;\n");
                    writer.write("                    } catch (Exception ignored) {}\n");
                    writer.write("                }\n");
                    writer.write("            }\n");
                    writer.write("            if (intrusiveField != null) {\n");
                    writer.write("                intrusiveField.setAccessible(true);\n");
                    writer.write("                if (intrusiveField.get(mapped) == null) {\n");
                    writer.write("                    intrusiveField.set(mapped, new java.util.IdentityHashMap<>());\n");
                    writer.write("                }\n");
                    writer.write("            }\n");
                    writer.write("        } catch (Exception e) {\n");
                    writer.write("            e.printStackTrace();\n");
                    writer.write("        }\n");
                    writer.write("    }\n\n");

                    writer.write("    private static void freeze(net.minecraft.core.Registry<?> registry) {\n");
                    writer.write("        if (registry instanceof net.minecraft.core.MappedRegistry<?>) {\n");
                    writer.write("            ((net.minecraft.core.MappedRegistry<?>) registry).freeze();\n");
                    writer.write("        }\n");
                    writer.write("    }\n");
                }

                writer.write("}\n");
            }
            return fullPath;
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate Loader: " + e.getMessage());
            return null;
        }
    }

    private void generateForgeRegistrar(Filer filer, String packageId, String className, String modId, List<RegistryFieldData> fields) {
        String generatedClassName = className + "_ForgeRegistrar";
        String fullPath = packageId + "." + generatedClassName;

        try {
            JavaFileObject fileObject = filer.createSourceFile(fullPath);
            try (Writer writer = fileObject.openWriter()) {
                writer.write("package " + packageId + ";\n\n");
                writer.write("import net.minecraftforge.eventbus.api.SubscribeEvent;\n");
                writer.write("import net.minecraftforge.fml.common.Mod;\n");
                writer.write("import net.minecraftforge.registries.RegisterEvent;\n");
                writer.write("import net.minecraft.core.registries.Registries;\n");
                writer.write("import net.minecraft.resources.ResourceLocation;\n");
                writer.write("import net.minecraft.resources.ResourceKey;\n\n");

                writer.write("@Mod.EventBusSubscriber(modid = \"" + modId + "\", bus = Mod.EventBusSubscriber.Bus.MOD)\n");
                writer.write("public class " + generatedClassName + " {\n\n");

                writer.write("    @SubscribeEvent\n");
                writer.write("    public static void onRegister(RegisterEvent event) {\n");

                for (RegistryFieldData field : fields) {
                    if (field.type == RegistryType.COMMAND) {
                        continue;
                    }
                    String rlExpr = "new ResourceLocation(\"" + getNamespace(field.id) + "\", \"" + getPath(field.id) + "\")";
                    String fieldRef = field.declaringClass + "." + field.name;

                    if (field.type == RegistryType.CUSTOM) {
                        String customRl = "new ResourceLocation(\"" + getNamespace(field.customRegistry) + "\", \"" + getPath(field.customRegistry) + "\")";
                        writer.write("        if (event.getRegistryKey().equals(ResourceKey.createRegistryKey(" + customRl + "))) {\n");
                        writer.write("            event.register(ResourceKey.createRegistryKey(" + customRl + "), " + rlExpr + ", () -> " + fieldRef + ");\n");
                        writer.write("        }\n");
                    } else {
                        String registryKey = getRegistryKeyName(field.type);
                        writer.write("        if (event.getRegistryKey().equals(Registries." + registryKey + ")) {\n");
                        writer.write("            event.register(Registries." + registryKey + ", " + rlExpr + ", () -> " + fieldRef + ");\n");
                        writer.write("        }\n");
                    }
                }

                writer.write("    }\n");
                writer.write("}\n");
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate Forge Registrar: " + e.getMessage());
        }
    }

    private void generateNeoForgeRegistrar(Filer filer, String packageId, String className, String modId, List<RegistryFieldData> fields) {
        String generatedClassName = className + "_NeoForgeRegistrar";
        String fullPath = packageId + "." + generatedClassName;

        try {
            JavaFileObject fileObject = filer.createSourceFile(fullPath);
            try (Writer writer = fileObject.openWriter()) {
                writer.write("package " + packageId + ";\n\n");
                writer.write("import net.neoforged.bus.api.SubscribeEvent;\n");
                writer.write("import net.neoforged.fml.common.EventBusSubscriber;\n");
                writer.write("import net.neoforged.neoforge.registries.RegisterEvent;\n");
                writer.write("import net.minecraft.core.registries.Registries;\n");
                writer.write("import net.minecraft.resources.ResourceLocation;\n");
                writer.write("import net.minecraft.resources.ResourceKey;\n\n");

                writer.write("@EventBusSubscriber(modid = \"" + modId + "\", bus = EventBusSubscriber.Bus.MOD)\n");
                writer.write("public class " + generatedClassName + " {\n\n");

                writer.write("    @SubscribeEvent\n");
                writer.write("    public static void onRegister(RegisterEvent event) {\n");

                for (RegistryFieldData field : fields) {
                    if (field.type == RegistryType.COMMAND) {
                        continue;
                    }
                    String rlExpr = "new ResourceLocation(\"" + getNamespace(field.id) + "\", \"" + getPath(field.id) + "\")";
                    String fieldRef = field.declaringClass + "." + field.name;

                    if (field.type == RegistryType.CUSTOM) {
                        String customRl = "new ResourceLocation(\"" + getNamespace(field.customRegistry) + "\", \"" + getPath(field.customRegistry) + "\")";
                        writer.write("        if (event.getRegistryKey().equals(ResourceKey.createRegistryKey(" + customRl + "))) {\n");
                        writer.write("            event.register(ResourceKey.createRegistryKey(" + customRl + "), " + rlExpr + ", () -> " + fieldRef + ");\n");
                        writer.write("        }\n");
                    } else {
                        String registryKey = getRegistryKeyName(field.type);
                        writer.write("        if (event.getRegistryKey().equals(Registries." + registryKey + ")) {\n");
                        writer.write("            event.register(Registries." + registryKey + ", " + rlExpr + ", () -> " + fieldRef + ");\n");
                        writer.write("        }\n");
                    }
                }

                writer.write("    }\n");
                writer.write("}\n");
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate NeoForge Registrar: " + e.getMessage());
        }
    }

    private void generateSpiFile(Filer filer) {
        try {
            FileObject serviceFile = filer.createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "META-INF/services/n.paradox.aslib.initializer.generator.IRegistryLoader"
            );
            try (Writer writer = serviceFile.openWriter()) {
                for (String registrar : generatedRegistrars) {
                    writer.write(registrar + "\n");
                }
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate SPI file: " + e.getMessage());
        }
    }

    private String getNamespace(String id) {
        if (id.contains(":")) {
            return id.substring(0, id.indexOf(':'));
        }
        return "minecraft";
    }

    private String getPath(String id) {
        if (id.contains(":")) {
            return id.substring(id.indexOf(':') + 1);
        }
        return id;
    }

    private String getBuiltInRegistryFieldName(RegistryType type) {
        return switch (type) {
            case BLOCK -> "BLOCK";
            case ITEM -> "ITEM";
            case SOUND_EVENT -> "SOUND_EVENT";
            case BLOCK_ENTITY_TYPE -> "BLOCK_ENTITY_TYPE";
            case ENTITY_TYPE -> "ENTITY_TYPE";
            case FLUID -> "FLUID";
            case CREATIVE_MODE_TAB -> "CREATIVE_MODE_TAB";
            default -> "BLOCK";
        };
    }

    private String getRegistryKeyName(RegistryType type) {
        return switch (type) {
            case BLOCK -> "BLOCK";
            case ITEM -> "ITEM";
            case SOUND_EVENT -> "SOUND_EVENT";
            case BLOCK_ENTITY_TYPE -> "BLOCK_ENTITY_TYPE";
            case ENTITY_TYPE -> "ENTITY_TYPE";
            case FLUID -> "FLUID";
            case CREATIVE_MODE_TAB -> "CREATIVE_MODE_TAB";
            default -> "BLOCK";
        };
    }

    private static class RegistryFieldData {
        final String declaringClass;
        final String name;
        final String id;
        final RegistryType type;
        final String customRegistry;

        RegistryFieldData(String declaringClass, String name, String id, RegistryType type, String customRegistry) {
            this.declaringClass = declaringClass;
            this.name = name;
            this.id = id;
            this.type = type;
            this.customRegistry = customRegistry;
        }
    }
}