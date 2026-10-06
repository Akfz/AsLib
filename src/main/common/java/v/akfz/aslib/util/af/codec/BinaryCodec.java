package v.akfz.aslib.util.af.codec;

import v.akfz.aslib.util.af.io.BinaryReader;
import v.akfz.aslib.util.af.io.BinaryWriter;
import v.akfz.aslib.util.af.schema.Schema;
import v.akfz.aslib.util.af.schema.SchemaBuilder;

import java.io.IOException;

public interface BinaryCodec<T> {

	void write(BinaryWriter writer, T value) throws IOException;
	T read(BinaryReader reader) throws IOException;

	/**
	 * Describes how the codec's output should be interpreted by the reader.
	 * <p>
	 * The default is an opaque {@code CUSTOM} node: the codec class, the value
	 * class, and version 0. Override if you want field-level migration to work
	 * inside your custom type — describe your fields as an {@code OBJECT} node.
	 */
	default Schema schema(Class<T> type) {
		return SchemaBuilder.customOnly(getClass(), type, 0);
	}
}