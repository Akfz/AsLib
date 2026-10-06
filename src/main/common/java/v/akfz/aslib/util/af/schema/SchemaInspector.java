package v.akfz.aslib.util.af.schema;

import v.akfz.aslib.util.af.BinaryException;
import v.akfz.aslib.util.af.BinaryHelper;
import v.akfz.aslib.util.af.io.BinaryReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Renders an .af file's schema as human-readable text — "the file describes
 * itself" made inspectable. Never touches the payload, so it works for any
 * class, registered or not.
 */
public final class SchemaInspector {

	private SchemaInspector() {}

	public static String inspect(byte[] fileBytes) throws IOException {
		BinaryReader r = new BinaryReader(new ByteArrayInputStream(fileBytes));

		byte[] magic = new byte[4];
		try { r.readFully(magic); }
		catch (IOException e) { throw new BinaryException("Truncated .af file"); }
		if (!Arrays.equals(magic,BinaryHelper.magic()))
			throw new BinaryException("Not an .af file (bad magic)");

		int version = r.readVarInt();
		if (version == 1) {
			return "AFB1 version=1 (pre-schema; not inspectable)\n";
		}
		if (version != 2)
			throw new BinaryException("Unsupported format version: " + version);

		int schemaLen = r.readVarInt();
		int payloadLen = r.readVarInt();
		byte[] schemaBytes = new byte[schemaLen];
		r.readFully(schemaBytes);

		Schema schema = Schema.read(new BinaryReader(new ByteArrayInputStream(schemaBytes)));

		StringBuilder sb = new StringBuilder(1024);
		sb.append("AFB1 version=").append(version)
				.append("  schemaLength=").append(schemaLen)
				.append("  payloadLength=").append(payloadLen).append('\n');
		sb.append('\n');

		sb.append("strings (").append(schema.pool.length).append("):\n");
		for (int i = 0; i < schema.pool.length; i++) {
			sb.append("  [").append(i).append("] ").append(schema.pool[i]).append('\n');
		}
		sb.append('\n');

		sb.append("types (").append(schema.types.length).append("):\n");
		for (int i = 0; i < schema.types.length; i++) {
			sb.append("  [").append(i).append("] ");
			formatType(sb, schema.types[i], schema);
			sb.append('\n');
		}
		sb.append('\n');

		sb.append("root: [").append(schema.rootIdx).append("]\n");
		return sb.toString();
	}

	private static void formatType(StringBuilder sb, TypeDef def, Schema schema) {
		sb.append(kindName(def.kind));
		switch (def.kind) {
			case TypeDef.K_ENUM -> {
				sb.append(' ').append(def.className).append(" { ");
				for (int i = 0; i < def.enumConstants.length; i++) {
					if (i > 0) sb.append(", ");
					sb.append(def.enumConstants[i]);
				}
				sb.append(" }");
			}
			case TypeDef.K_ARRAY ->
					sb.append(" elem=[").append(def.elemIdx).append(']');
			case TypeDef.K_COLLECTION -> {
				sb.append(' ').append(def.className);
				sb.append(" elem=[").append(def.elemIdx).append(']');
			}
			case TypeDef.K_MAP -> {
				sb.append(' ').append(def.className);
				sb.append(" key=[").append(def.keyIdx).append(']');
				sb.append(" val=[").append(def.valIdx).append(']');
			}
			case TypeDef.K_OBJECT -> {
				sb.append(' ').append(def.className).append(" {\n");
				for (int i = 0; i < def.fieldNames.length; i++) {
					int ref = def.fieldTypeIdx[i];
					TypeDef r = schema.types[ref];
					sb.append("        ").append(def.fieldNames[i])
							.append(" : [").append(ref).append("] ")
							.append(kindName(r.kind));
					if (r.className != null) sb.append(' ').append(r.className);
					sb.append('\n');
				}
				sb.append("    }");
			}
			case TypeDef.K_CUSTOM -> {
				sb.append(" codec=").append(def.codecClass);
				sb.append(" value=").append(def.className);
				sb.append(" version=").append(def.codecVersion);
			}
			default -> { /* primitives: nothing */ }
		}
	}

	static String kindName(byte kind) {
		return switch (kind) {
			case TypeDef.K_BOOLEAN -> "BOOLEAN";
			case TypeDef.K_BYTE    -> "BYTE";
			case TypeDef.K_SHORT   -> "SHORT";
			case TypeDef.K_INT     -> "INT";
			case TypeDef.K_LONG    -> "LONG";
			case TypeDef.K_FLOAT   -> "FLOAT";
			case TypeDef.K_DOUBLE  -> "DOUBLE";
			case TypeDef.K_CHAR    -> "CHAR";
			case TypeDef.K_BOOLEAN_BOX -> "BOOLEAN?";
			case TypeDef.K_BYTE_BOX    -> "BYTE?";
			case TypeDef.K_SHORT_BOX   -> "SHORT?";
			case TypeDef.K_INT_BOX     -> "INT?";
			case TypeDef.K_LONG_BOX    -> "LONG?";
			case TypeDef.K_FLOAT_BOX   -> "FLOAT?";
			case TypeDef.K_DOUBLE_BOX  -> "DOUBLE?";
			case TypeDef.K_CHAR_BOX    -> "CHAR?";
			case TypeDef.K_STRING -> "STRING";
			case TypeDef.K_UUID   -> "UUID";
			case TypeDef.K_BYTES  -> "BYTES";
			case TypeDef.K_ENUM       -> "ENUM";
			case TypeDef.K_ARRAY      -> "ARRAY";
			case TypeDef.K_COLLECTION -> "COLLECTION";
			case TypeDef.K_MAP        -> "MAP";
			case TypeDef.K_OBJECT     -> "OBJECT";
			case TypeDef.K_CUSTOM     -> "CUSTOM";
			default -> "KIND_0x" + Integer.toHexString(kind & 0xFF);
		};
	}
}