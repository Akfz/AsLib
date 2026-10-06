package v.akfz.aslib.util.af;

import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.util.af.codec.BinaryCodec;
import v.akfz.aslib.util.af.io.BinaryReader;
import v.akfz.aslib.util.af.io.BinaryWriter;
import v.akfz.aslib.util.af.registry.BinaryRegistry;
import v.akfz.aslib.util.af.schema.Schema;
import v.akfz.aslib.util.af.schema.SchemaInspector;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Binary file format (.af) — schema-first, versioned, migration-aware.
 * <p>
 * Layout:
 * <pre>
 * AFB1
 * formatVersion : varint   // = 2
 * schemaLength  : varint
 * payloadLength : varint
 * schema        : schemaLength bytes
 * payload       : payloadLength bytes
 * </pre>
 * <p>
 * The schema describes the shape of the payload (field names, types, enum
 * constants, custom codec classes) and is always read first. Reading a file
 * whose schema differs from the current one migrates field-by-field: names in
 * the file are matched against names in the class, missing fields are dropped,
 * new fields keep their constructor defaults.
 * <p>
 * Format version 1 (pre-schema) is not readable. {@link #read} throws
 * {@link IncompatibleFormatException}; {@link #readOrNull} and
 * {@link #readOrDefault} swallow it. {@link #readAndMigrate} backs up the old
 * file and rewrites it with the current schema.
 */
public final class BinaryHelper {

	private static final byte[] MAGIC = {'A', 'F', 'B', '1'};
	public static final int FORMAT_VERSION = 2;

	private static final DateTimeFormatter OLD_TS =
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm");

	private BinaryHelper() {}

	public static byte[] magic() {
		return MAGIC.clone();
	}

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
		Schema schema = codec.schema(type);

		ByteArrayOutputStream payloadBuf = new ByteArrayOutputStream();
		BinaryWriter pw = new BinaryWriter(payloadBuf);
		schema.writeValue(pw, value, schema.rootIdx, true);
		pw.flush();
		byte[] payload = payloadBuf.toByteArray();

		ByteArrayOutputStream schemaBuf = new ByteArrayOutputStream();
		BinaryWriter sw = new BinaryWriter(schemaBuf);
		schema.write(sw);
		sw.flush();
		byte[] schemaBytes = schemaBuf.toByteArray();

		out.write(MAGIC);
		BinaryWriter w = new BinaryWriter(out);
		w.writeVarInt(FORMAT_VERSION);
		w.writeVarInt(schemaBytes.length);
		w.writeVarInt(payload.length);
		w.writeRaw(schemaBytes);
		w.writeRaw(payload);
		out.flush();
	}

	public static String inspect(Path path) throws IOException {
		return SchemaInspector.inspect(Files.readAllBytes(path));
	}

	public static String inspect(File file) throws IOException {
		return inspect(file.toPath());
	}

	public static String inspect(InputStream in) throws IOException {
		return SchemaInspector.inspect(in.readAllBytes());
	}

	public static <T> T read(Path path, Class<T> type) throws IOException {
		try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
			return read(in, type);
		}
	}

	public static <T> T read(File file, Class<T> type) throws IOException {
		return read(file.toPath(), type);
	}

	@Nullable
	public static <T> T readOrNull(Path path, Class<T> type) throws IOException {
		try {
			return read(path, type);
		} catch (IncompatibleFormatException e) {
			return null;
		}
	}

	public static <T> T readOrDefault(Path path, Class<T> type, T def) throws IOException {
		T t = readOrNull(path, type);
		return t != null ? t : def;
	}

	@SuppressWarnings("unchecked")
	public static <T> T read(InputStream in, @Nullable Class<T> expected) throws IOException {
		BinaryReader r = new BinaryReader(in);

		byte[] magic = new byte[4];
		r.readFully(magic);
		if (!Arrays.equals(magic, MAGIC))
			throw new BinaryException("Not an .af file (bad magic)");

		int version = r.readVarInt();
		if (version == 1)
			throw new IncompatibleFormatException(
					"AF format version 1 (pre-schema) is not readable by this build");
		if (version != FORMAT_VERSION)
			throw new BinaryException("Unsupported format version: " + version);

		int schemaLen = r.readVarInt();
		int payloadLen = r.readVarInt();

		byte[] schemaBytes = new byte[schemaLen];
		r.readFully(schemaBytes);
		byte[] payloadBytes = new byte[payloadLen];
		r.readFully(payloadBytes);

		Schema schema = Schema.read(new BinaryReader(new ByteArrayInputStream(schemaBytes)));

		Object result = schema.readValue(
				new BinaryReader(new ByteArrayInputStream(payloadBytes)),
				schema.rootIdx,
				expected,
				true);

		if (expected != null) return expected.cast(result);
		return (T) result;
	}

	@Nullable
	public static <T> T read(InputStream in) throws IOException {
		return read(in, null);
	}

	/**
	 * Reads the file; if its schema differs from the current one, backs the
	 * original up and rewrites it using the current schema.
	 * <p>
	 * Format v1 files cannot be read at all: they are backed up (or deleted)
	 * and {@code null} is returned.
	 *
	 * @param saveOld keep the backup ({@code name.yyyy-MM-ddTHH-mm.old}) instead of deleting it
	 * @return the migrated object, or {@code null} if the file was unreadable legacy
	 */
	public static <T> T readAndMigrate(Path path, Class<T> type, boolean saveOld) throws IOException {
		if (!Files.exists(path)) return null;

		byte[] data = Files.readAllBytes(path);
		BinaryReader r = new BinaryReader(new ByteArrayInputStream(data));

		byte[] magic = new byte[4];
		try { r.readFully(magic); }
		catch (EOFException e) { throw new BinaryException("Truncated .af file: " + path); }
		if (!Arrays.equals(magic, MAGIC))
			throw new BinaryException("Not an .af file (bad magic): " + path);

		int version = r.readVarInt();
		if (version == 1) {
			Path backup = uniqueOldPath(path);
			Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
			if (!saveOld) Files.deleteIfExists(backup);
			return null;
		}
		if (version != FORMAT_VERSION)
			throw new BinaryException("Unsupported format version: " + version);

		int schemaLen = r.readVarInt();
		int payloadLen = r.readVarInt();
		byte[] schemaBytes = new byte[schemaLen];
		r.readFully(schemaBytes);
		byte[] payloadBytes = new byte[payloadLen];
		r.readFully(payloadBytes);

		Schema fileSchema = Schema.read(new BinaryReader(new ByteArrayInputStream(schemaBytes)));
		BinaryCodec<T> codec = BinaryRegistry.codecFor(type);
		Schema currentSchema = codec.schema(type);

		Object result = fileSchema.readValue(
				new BinaryReader(new ByteArrayInputStream(payloadBytes)),
				fileSchema.rootIdx,
				type,
				true);

		if (fileSchema.equals(currentSchema)) {
			return type.cast(result);
		}

		Path backup = uniqueOldPath(path);
		Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
		try {
			write(path, type, type.cast(result));
		} catch (IOException | RuntimeException e) {
			Files.move(backup, path, StandardCopyOption.REPLACE_EXISTING);
			throw e;
		} finally {
			if (!saveOld) Files.deleteIfExists(backup);
		}
		return type.cast(result);
	}

	private static Path uniqueOldPath(Path path) {
		String ts = LocalDateTime.now().format(OLD_TS);
		String base = path.getFileName() + "." + ts;
		Path p = path.resolveSibling(base + ".old");
		if (!Files.exists(p)) return p;
		int n = 2;
		while (true) {
			p = path.resolveSibling(base + "-" + n + ".old");
			if (!Files.exists(p)) return p;
			n++;
		}
	}
}