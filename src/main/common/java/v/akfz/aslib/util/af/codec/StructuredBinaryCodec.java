package v.akfz.aslib.util.af.codec;

import v.akfz.aslib.util.af.io.BinaryReader;
import v.akfz.aslib.util.af.io.BinaryWriter;
import v.akfz.aslib.util.af.schema.Schema;

import java.io.IOException;

/**
 * A {@link BinaryCodec} that declares its own wire format via
 * {@link #schema(Class)} and reads/writes fields one by one, so that
 * field-level migration works <i>inside</i> the codec's type.
 * <p>
 * Contrast with a plain {@code BinaryCodec}, whose schema defaults to a single
 * {@code CUSTOM} node: the value is a black box on the wire, and the framework
 * can neither migrate fields inside it nor skip past individual fields.
 * <p>
 * Contract:
 * <ul>
 *   <li>{@link #schema(Class)} must return a non-{@code CUSTOM} root.</li>
 *   <li>{@link #writeFields} writes exactly the fields of the schema returned
 *       by {@link #schema(Class)}, in the declared order, using
 *       {@link Schema#writeValue} for each.</li>
 *   <li>{@link #readFields} reads using the schema stored in the file. When the
 *       file schema differs from the current one, this method is responsible
 *       for the mapping: match by field name, drop unknown fields, default
 *       missing ones.</li>
 * </ul>
 * <p>
 * Direct calls to {@link #write}/{@link #read} are optional; the schema-driven
 * pipeline never uses them.
 */
public interface StructuredBinaryCodec<T> extends BinaryCodec<T> {

	@Override
	Schema schema(Class<T> type);

	/**
	 * Writes {@code value} using {@code schema}. The {@code schema} argument is
	 * exactly what {@link #schema(Class)} returned for {@code value.getClass()}.
	 */
	void writeFields(BinaryWriter writer, T value, Schema schema, int typeIdx) throws IOException;

	/**
	 * Reads a value using {@code fileSchema}, which is what was stored in the
	 * file and may not match the current schema. {@code type} is the class the
	 * codec is registered for, passed for logging / diagnostics only — the
	 * return type {@code T} already guarantees compatibility.
	 */
	T readFields(BinaryReader reader, Schema fileSchema, int fileTypeIdx, Class<?> type)
			throws IOException;
}