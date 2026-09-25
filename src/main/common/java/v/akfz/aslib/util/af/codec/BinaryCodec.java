package v.akfz.aslib.util.af.codec;

import v.akfz.aslib.util.af.io.BinaryReader;
import v.akfz.aslib.util.af.io.BinaryWriter;

import java.io.IOException;

public interface BinaryCodec<T> {
	void write(BinaryWriter writer, T value) throws IOException;
	T read(BinaryReader reader) throws IOException;
}