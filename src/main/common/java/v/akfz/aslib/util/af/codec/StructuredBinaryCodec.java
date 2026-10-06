package v.akfz.aslib.util.af.codec;

import v.akfz.aslib.util.af.io.BinaryReader;
import v.akfz.aslib.util.af.io.BinaryWriter;
import v.akfz.aslib.util.af.schema.Schema;

import java.io.IOException;

/**
 * A {@link BinaryCodec} that declares its own wire format via
 * {@link #schema(Class)} and reads/writes fields one by one, so field-level
 * migration works inside the codec's type. A plain {@link BinaryCodec} is a
 * {@code CUSTOM} black box instead — no per-field migration, no skipping.
 * <p>
 * Inside {@link #writeFields}/{@link #readFields} always pass
 * {@code topLevel=false} when delegating to {@link Schema#writeValue} /
 * {@link Schema#readValue}; only the root value is unframed.
 */
public interface StructuredBinaryCodec<T> extends BinaryCodec<T> {

	@Override
	Schema schema(Class<T> type);

	/**
	 * Writes {@code value} according to {@code schema}, in the declared field
	 * order.
	 */
	void writeFields(BinaryWriter writer, T value, Schema schema, int typeIdx) throws IOException;

	/**
	 * Reads a value using {@code fileSchema}. If it differs from the current
	 * schema, this method maps by field name: unknown fields dropped, missing
	 * ones defaulted. {@code type} is for diagnostics only.
	 */
	T readFields(BinaryReader reader, Schema fileSchema, int fileTypeIdx, Class<?> type)
			throws IOException;
}