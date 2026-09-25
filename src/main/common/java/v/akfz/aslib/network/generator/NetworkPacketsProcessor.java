package v.akfz.aslib.network.generator;

import v.akfz.aslib.network.annotation.NetworkPacket;

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
import java.util.LinkedHashSet;
import java.util.Set;

@SupportedAnnotationTypes("v.akfz.aslib.network.annotation.NetworkPacket")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class NetworkPacketsProcessor extends AbstractProcessor {

	private static final String SELF_HANDLED = "v.akfz.aslib.network.api.SelfHandledPacket";
	private static final String REGISTRAR_INTERFACE = "v.akfz.aslib.network.registry.IPacketRegistrar";
	private static final String NETWORKING_CLASS = "v.akfz.aslib.network.AsLibNetworking";
	private static final String HANDLER_INTERFACE = "v.akfz.aslib.network.api.PacketHandler";

	private boolean generated = false;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver() || generated) return true;

		Set<? extends Element> packets = roundEnv.getElementsAnnotatedWith(NetworkPacket.class);
		if (packets.isEmpty()) return true;

		TypeElement anchor = null;
		TypeElement generateRegistries = processingEnv.getElementUtils()
				.getTypeElement("v.akfz.aslib.initializer.generator.GenerateRegistries");
		if (generateRegistries != null) {
			outer:
			for (Element e : roundEnv.getRootElements()) {
				if (!(e instanceof TypeElement te)) continue;
				if (te.getAnnotationMirrors().stream().anyMatch(am ->
						am.getAnnotationType().asElement().equals(generateRegistries))) {
					anchor = te;
					break outer;
				}
			}
		}

		if (anchor == null) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
					"NetworkPacketsProcessor: no @GenerateRegistries class found — "
							+ "packet autoregistration skipped.");
			return true;
		}

		String pkg = processingEnv.getElementUtils().getPackageOf(anchor).getQualifiedName().toString();
		String anchorName = anchor.getSimpleName().toString();
		String generatedName = anchorName + "_AutoPacketRegistrar";
		String fullName = pkg.isEmpty() ? generatedName : pkg + "." + generatedName;

		LinkedHashSet<String> lines = new LinkedHashSet<>();
		for (Element e : packets) {
			if (!(e instanceof TypeElement te)) continue;
			NetworkPacket np = te.getAnnotation(NetworkPacket.class);
			if (np == null || !np.autoreg()) continue;

			String fqcn = te.getQualifiedName().toString();
			boolean selfHandled = implementsSelfHandled(te);

			if (selfHandled) {
				lines.add(String.format(
						"        { %s p = new %s(); %s.REGISTRY.register(p, p.handler()); }",
						fqcn, fqcn, NETWORKING_CLASS));
			} else {
				lines.add(String.format(
						"        %s.REGISTRY.register(new %s(), new %s<%s>() {});",
						NETWORKING_CLASS, fqcn, HANDLER_INTERFACE, fqcn));
			}
		}

		if (lines.isEmpty()) return true;

		try {
			JavaFileObject file = processingEnv.getFiler().createSourceFile(fullName, anchor);
			try (Writer w = file.openWriter()) {
				if (!pkg.isEmpty()) w.write("package " + pkg + ";\n\n");
				w.write("public class " + generatedName + " implements " + REGISTRAR_INTERFACE + " {\n");
				w.write("    @Override public void register() {\n");
				for (String line : lines) w.write(line + "\n");
				w.write("    }\n");
				w.write("}\n");
			}

			FileObject service = processingEnv.getFiler().createResource(
					StandardLocation.CLASS_OUTPUT, "",
					"META-INF/services/" + REGISTRAR_INTERFACE);
			try (Writer w = service.openWriter()) {
				w.write(fullName + "\n");
			}

			generated = true;
		} catch (Exception ex) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
					"Failed to generate packet registrar: " + ex.getMessage());
		}

		return true;
	}

	private boolean implementsSelfHandled(TypeElement te) {
		Types types = processingEnv.getTypeUtils();
		TypeElement selfHandled = processingEnv.getElementUtils().getTypeElement(SELF_HANDLED);
		if (selfHandled == null) return false;
		TypeMirror target = types.erasure(selfHandled.asType());

		TypeMirror cursor = te.asType();
		while (cursor instanceof javax.lang.model.type.DeclaredType dt
				&& dt.asElement() instanceof TypeElement t) {
			for (TypeMirror iface : t.getInterfaces()) {
				if (types.isAssignable(types.erasure(iface), target)) return true;
			}
			cursor = t.getSuperclass();
		}
		return false;
	}
}