package n.paradox.aslib.annotation.processor;

import n.paradox.aslib.annotation.DontCompile;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes({
        "n.paradox.aslib.annotation.DontCompile"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class DontCompileProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Filer filer = processingEnv.getFiler();

        Set<? extends Element> dontCompileElements = roundEnv.getElementsAnnotatedWith(DontCompile.class);
        for (Element element : dontCompileElements) {
            if (element instanceof TypeElement typeElement) {
                String qualifiedName = typeElement.getQualifiedName().toString();
                String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();
                String simpleName = typeElement.getSimpleName().toString();

                String currentTarget = processingEnv.getOptions().get("modLoaderTarget");

                if (currentTarget != null) {
                    try {
                        JavaFileObject blankFile = filer.createSourceFile(qualifiedName);
                        try (Writer writer = blankFile.openWriter()) {
                            writer.write("package " + packageName + ";\n\n");
                            writer.write("/** Сгенерировано автоматически. Оригинальный dev-код вырезан. */\n");
                            writer.write("public final class " + simpleName + " {\n");
                            writer.write("    // Пусто. Весь код удален процессором аннотаций.\n");
                            writer.write("}\n");
                        }
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "[AsLib] Код класса " + simpleName + " успешно вырезан в релизе.");
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return true;
    }
}