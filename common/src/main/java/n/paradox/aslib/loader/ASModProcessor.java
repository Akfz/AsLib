package n.paradox.aslib.loader;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("n.paradox.aslib.loader.ASMod")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ASModProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(ASMod.class)) {
            if (element instanceof TypeElement typeElement) {
                ASMod annotation = typeElement.getAnnotation(ASMod.class);
                String modId = annotation.modID();
                boolean isClient = annotation.isClient();

                String fullClassName = typeElement.getQualifiedName().toString();
                String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
                String simpleClassName = typeElement.getSimpleName().toString();

                generateFabricBridge(packageName, simpleClassName, fullClassName, isClient);
                generateForgeBridge(packageName, simpleClassName, fullClassName, modId, isClient);
            }
        }
        return true;
    }

    private void generateFabricBridge(String pkg, String className, String fullPath, boolean isClient) {
        String suffix = isClient ? "FabricClientBridge" : "FabricBridge";
        String interfaceName = isClient ? "ClientModInitializer" : "ModInitializer";
        String methodName = isClient ? "onInitializeClient" : "onInitialize";

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(pkg + "." + className + suffix);
            try (Writer writer = file.openWriter()) {
                writer.write("package " + pkg + ";\n\n");
                writer.write("import net.fabricmc.api." + (isClient ? "ClientModInitializer" : "ModInitializer") + ";\n\n");
                writer.write("public class " + className + suffix + " implements " + interfaceName + " {\n");
                writer.write("    @Override\n");
                writer.write("    public void " + methodName + "() {\n");
                writer.write("        " + fullPath + " mod = new " + fullPath + "();\n");
                writer.write("        mod.preInit();\n");
                writer.write("        mod.Init();\n");
                writer.write("    }\n");
                writer.write("}\n");
            }
        } catch (Exception ignored) {}
    }

    private void generateForgeBridge(String pkg, String className, String fullPath, String modId, boolean isClient) {
        String suffix = isClient ? "ForgeClientBridge" : "ForgeBridge";
        String eventClass = isClient ? "net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent" : "net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent";

        try {
            JavaFileObject file = processingEnv.getFiler().createSourceFile(pkg + "." + className + suffix);
            try (Writer writer = file.openWriter()) {
                writer.write("package " + pkg + ";\n\n");
                writer.write("import net.minecraftforge.fml.common.Mod;\n");
                writer.write("import net.minecraftforge.javafmlmod.FMLJavaModLoadingContext;\n\n");
                writer.write("@Mod(\"" + modId + "\")\n");
                writer.write("public class " + className + suffix + " {\n");
                writer.write("    private final " + fullPath + " mod = new " + fullPath + "();\n\n");
                writer.write("    public " + className + suffix + "() {\n");
                writer.write("        mod.preInit();\n");
                writer.write("        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);\n");
                writer.write("    }\n\n");
                writer.write("    private void init(final " + eventClass + " event) {\n");
                writer.write("        mod.Init();\n");
                writer.write("    }\n");
                writer.write("}\n");
            }
        } catch (Exception ignored) {}
    }
}