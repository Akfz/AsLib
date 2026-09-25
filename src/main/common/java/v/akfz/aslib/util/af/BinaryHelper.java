package v.akfz.aslib.util.af;

import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.util.af.codec.BinaryCodec;
import v.akfz.aslib.util.af.io.BinaryReader;
import v.akfz.aslib.util.af.io.BinaryWriter;
import v.akfz.aslib.util.af.registry.BinaryRegistry;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Binary file format (.af) — small, fast, no BS.
 * <p>
 * Its replacement for {@link v.akfz.aslib.util.json.GsonHelper} when
 * human-readability doesn't matter and size/speed do.
 * <p>
 * Format is dead simple:
 * <pre>
 * AFB1                      // magic
 * formatVersion : varint
 * flags         : byte      // reserved (compression, dictionary, etc.)
 * compression   : byte      // 0 = none
 * className     : string    // FQCN of the top-level value's class
 * payloadLength : varint
 * payload       : raw codec bytes
 * </pre>
 * <p>
 * Classes don't serialize themselves — you write a {@link BinaryCodec} for each
 * type and register it in {@link BinaryRegistry}. If you don't, an
 * {@link v.akfz.aslib.util.af.codec.BinaryAutoCodec} is created on the fly.
 * <p>
 * <b>Register your codec once:</b>
 * <pre>{@code
 * BinaryRegistry.register(Settings.class, new SettingsCodec());
 * }</pre>
 * <p>
 * <b>Write / read:</b>
 * <pre>{@code
 * Settings s = new Settings(2, 0.75f, "hello");
 * BinaryHelper.write(Path.of("config.af"), s);
 *
 * Settings back = BinaryHelper.read(Path.of("config.af"), Settings.class);
 * }</pre>
 * <p>
 * <b>Nested objects</b> — from inside your own codec, just call the codec for
 * the field's declared type. {@link BinaryRegistry#codecFor(Class)} gives you
 * the right one (custom if registered, {@code AutoCodec} otherwise):
 * <pre>{@code
 * public void write(BinaryWriter w, Profile p) throws IOException {
 *     w.writeString(p.nick);
 *     BinaryRegistry.codecFor(Settings.class).write(w, p.settings);
 * }
 * }</pre>
 * <p>
 * <b>Streams work too</b> if you don't want files:
 * <pre>{@code
 * ByteArrayOutputStream buf = new ByteArrayOutputStream();
 * BinaryHelper.write(buf, myThing);
 *
 * MyThing loaded = BinaryHelper.read(new ByteArrayInputStream(buf.toByteArray()), MyThing.class);
 * }</pre>
 * <p>
 * <b>Notes:</b>
 * <ul>
 *   <li>compression, fieldId, string dictionary — v2 problems, flags are reserved</li>
 *   <li>class name in the file means renaming a class breaks old files — tradeoff</li>
 *   <li>no limits on string/collection size yet — don't feed it untrusted bytes</li>
 *   <li>{@link BinaryException} is a RuntimeException on purpose, codecs shouldn't
 *    force you to wrap every line in try/catch</li>
 * </ul>
 */
public final class BinaryHelper {

	private static final byte[] MAGIC = {'A', 'F', 'B', '1'};
	private static final int FORMAT_VERSION = 1;

	public static final byte COMPRESSION_NONE = 0;

	private BinaryHelper() {}

	public static <T> void write(Path path, T value) throws IOException {
		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
			write(out, value);
		}
	}

	public static <T> void write(File file, T value) throws IOException {
		write(file.toPath(), value);
	}

	public static <T> void write(Path path, Class<T> type, T value) throws IOException {
		try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
			write(out, type, value);
		}
	}

	public static <T> void write(OutputStream out, T value) throws IOException {
		if (value == null) throw new NullPointerException("value");
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>) value.getClass();
		write(out, type, value);
	}

	public static <T> void write(OutputStream out, Class<T> type, T value) throws IOException {
		if (type == null) throw new NullPointerException("type");
		if (value == null) throw new NullPointerException("value");

		BinaryCodec<T> codec = BinaryRegistry.codecFor(type);
		byte[] payload = BinaryWriter.capture(codec, value);

		out.write(MAGIC);
		BinaryWriter w = new BinaryWriter(out);
		w.writeVarInt(FORMAT_VERSION);
		w.writeByte(0);
		w.writeByte(COMPRESSION_NONE);
		w.writeString(type.getName());
		w.writeVarInt(payload.length);
		out.write(payload);
		out.flush();
	}

	public static <T> T read(Path path, Class<T> type) throws IOException {
		try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
			return read(in, type);
		}
	}

	public static <T> T read(File file, Class<T> type) throws IOException {
		return read(file.toPath(), type);
	}

	@SuppressWarnings("unchecked")
	public static <T> T read(InputStream in, @Nullable Class<T> expected) throws IOException {
		BinaryReader r = new BinaryReader(in);
		byte[] magic = new byte[4];
		r.readFully(magic);
		if (!Arrays.equals(magic, MAGIC)) {
			throw new BinaryException("Not an .af file (bad magic)");
		}
		int version = r.readVarInt();
		if (version > FORMAT_VERSION) {
			throw new BinaryException("Unsupported format version: " + version);
		}
		r.readByte();
		int compression = r.readByte();
		if (compression != COMPRESSION_NONE) {
			throw new BinaryException("Compression not supported: " + compression);
		}
		String className = r.readString();
		if (className == null) throw new BinaryException("Missing class name in payload header");
		int payloadLength = r.readVarInt();
		byte[] payload = new byte[payloadLength];
		r.readFully(payload);

		Class<?> type;
		try {
			type = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new BinaryException("Class not found: " + className, e);
		}
		if (expected != null && !expected.isAssignableFrom(type)) {
			throw new BinaryException("Type mismatch: expected " + expected.getName()
					+ ", got " + type.getName());
		}

		BinaryCodec<Object> codec = BinaryRegistry.codecFor((Class<Object>) type);
		BinaryReader pr = new BinaryReader(new ByteArrayInputStream(payload));
		return (T) codec.read(pr);
	}

	@Nullable
	public static <T> T read(InputStream in) throws IOException {
		return read(in, null);
	}
}