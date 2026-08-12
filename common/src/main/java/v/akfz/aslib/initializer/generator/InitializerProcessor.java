package v.akfz.aslib.initializer.generator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("v.akfz.aslib.initializer.generator.GenerateInitializer")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class InitializerProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Filer filer = processingEnv.getFiler();

        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateInitializer.class)) {
            if (element instanceof TypeElement typeElement) {
                GenerateInitializer annotation = typeElement.getAnnotation(GenerateInitializer.class);

                LoaderType loader = annotation.loader();
                String modId = annotation.modId();
                boolean isClient = annotation.isClient();
                String packageId = annotation.packageStr();
                String addClassFg = annotation.addClassNameForge();
                String addClassFb = annotation.addClassNameFabric();

                String mainClassCanonicalName = null;
                try {
                    mainClassCanonicalName = annotation.mainClass().getCanonicalName();
                } catch (MirroredTypeException mte) {
                    TypeMirror typeMirror = mte.getTypeMirror();
                    mainClassCanonicalName = typeMirror.toString();
                }

                String mainClassSimpleName;

                if (mainClassCanonicalName.equals("v.akfz.aslib.initializer.generator.NotAClass")) {
                    mainClassCanonicalName = typeElement.getQualifiedName().toString();
                    mainClassSimpleName = typeElement.getSimpleName().toString();
                } else {
                    mainClassSimpleName = mainClassCanonicalName.substring(mainClassCanonicalName.lastIndexOf('.') + 1);
                }

                String endPackage;
                if (packageId.equals("null")) {
                    endPackage = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
                } else {
                    endPackage = packageId;
                }

                String currentTarget = processingEnv.getOptions().get("modLoaderTarget");
                if (currentTarget != null) {
                    if (currentTarget.equalsIgnoreCase("fabric") && loader == LoaderType.ForgeLike) {
                        continue;
                    }
                    if ((currentTarget.equalsIgnoreCase("forge") || currentTarget.equalsIgnoreCase("neoforge")) && loader == LoaderType.FabricLike) {
                        continue;
                    }
                }

                if (loader == LoaderType.ForgeLike) {
                    generateForgeInitializer(filer, mainClassCanonicalName, mainClassSimpleName, modId, isClient, endPackage, addClassFg);
                } else if (loader == LoaderType.FabricLike) {
                    generateFabricInitializer(filer, mainClassCanonicalName, mainClassSimpleName, isClient, endPackage, addClassFb);
                } else {
                    if (currentTarget == null) return true;// теперь в common не компилирует fabric и forge (они не работают все равно)
                    if (currentTarget.equalsIgnoreCase("fabric")) {
                        generateFabricInitializer(filer, mainClassCanonicalName, mainClassSimpleName, isClient, endPackage, addClassFb);
                    } else if (currentTarget.equalsIgnoreCase("forge") || currentTarget.equalsIgnoreCase("neoforge")) {
                        generateForgeInitializer(filer, mainClassCanonicalName, mainClassSimpleName, modId, isClient, endPackage, addClassFg);
                    }
                }
            }
        }
        return true;
    }

    private void generateForgeInitializer(Filer filer, String mainClassPath, String mainClassName, String modId, boolean isClient,
                                          String packageid, String addClassName) {

        String generatedClassName = mainClassName + addClassName;
        String fullPath = packageid + "." + generatedClassName;

        try {
            JavaFileObject fileObject = filer.createSourceFile(fullPath);
            try (Writer writer = fileObject.openWriter()) {
                writer.write("package " + packageid + ";\n\n");
                writer.write("import " + mainClassPath + ";\n");
                writer.write("import net.minecraftforge.fml.common.Mod;\n");
                if (isClient) {
                    writer.write("import net.minecraftforge.api.distmarker.Dist;\n");
                    writer.write("import net.minecraftforge.fml.loading.FMLEnvironment;\n");
                }
                writer.write("\n");

                writer.write("@Mod(\"" + modId + "\")\n");
                writer.write("public class " + generatedClassName + " {\n");
                writer.write("    private static final " + mainClassName + " MAININSTANCE = new " + mainClassName + "();\n\n");

                writer.write("    public static " + mainClassName + " getInstance() {\n");
                writer.write("        return MAININSTANCE;\n");
                writer.write("    }\n\n");

                writer.write("    public " + generatedClassName + "() {\n");
                if (isClient) {
                    writer.write("        if (FMLEnvironment.dist == Dist.CLIENT) {\n");
                    writer.write("            MAININSTANCE.init();\n");
                    writer.write("        }\n");
                } else {
                    writer.write("        MAININSTANCE.init();\n");
                }
                writer.write("    }\n");
                writer.write("}\n");
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Ошибка генерации Forge Initializer (" + generatedClassName + "): " + e.getMessage());
        }
    }

    private void generateFabricInitializer(Filer filer, String mainClassPath, String mainClassName, boolean isClient,
                                           String packageid, String addClassName) {

        String generatedClassName = mainClassName + addClassName;
        String fullPath = packageid + "." + generatedClassName;

        String fabricInterface = isClient ? "net.fabricmc.api.ClientModInitializer" : "net.fabricmc.api.ModInitializer";
        String methodName = isClient ? "onInitializeClient" : "onInitialize";

        try {
            JavaFileObject fileObject = filer.createSourceFile(fullPath);
            try (Writer writer = fileObject.openWriter()) {
                writer.write("package " + packageid + ";\n\n");
                writer.write("import " + mainClassPath + ";\n\n");

                writer.write("public class " + generatedClassName + " implements " + fabricInterface + " {\n");
                writer.write("    private static final " + mainClassName + " MAININSTANCE = new " + mainClassName + "();\n\n");

                writer.write("    public static " + mainClassName + " getInstance() {\n");
                writer.write("        return MAININSTANCE;\n");
                writer.write("    }\n\n");

                writer.write("    @Override\n");
                writer.write("    public void " + methodName + "() {\n");
                writer.write("        MAININSTANCE.init();\n");
                writer.write("    }\n");
                writer.write("}\n");
            }
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Ошибка HTML генерации Fabric Initializer (" + generatedClassName + "): " + e.getMessage());
        }
    }
}