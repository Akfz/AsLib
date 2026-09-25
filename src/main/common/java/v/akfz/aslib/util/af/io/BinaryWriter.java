package v.akfz.aslib.util.af.io;

import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.util.af.codec.BinaryCodec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Low-level byte writer for the AF format. Little-endian where it matters,
 * VarInt for ints/longs (zigzag). Nullable values get a 1-byte prefix.
 */
public final class BinaryWriter {
	private final OutputStream out;

	public BinaryWriter(OutputStream out) {
		this.out = out;
	}

	public void writeByte(int b) throws IOException {
		out.write(b & 0xFF);
	}

	public void writeRaw(byte[] bytes) throws IOException {
		out.write(bytes);
	}

	public void writeBoolean(boolean v) throws IOException {
		out.write(v ? 1 : 0);
	}

	public void writeVarInt(int value) throws IOException {
		while ((value & ~0x7F) != 0) {
			out.write((value & 0x7F) | 0x80);
			value >>>= 7;
		}
		out.write(value);
	}

	public void writeVarLong(long value) throws IOException {
		while ((value & ~0x7FL) != 0) {
			out.write((int) ((value & 0x7F) | 0x80));
			value >>>= 7;
		}
		out.write((int) value);
	}

	public void writeInt(int v) throws IOException {
		writeVarInt((v << 1) ^ (v >> 31));
	}

	public void writeLong(long v) throws IOException {
		writeVarLong((v << 1) ^ (v >> 63));
	}

	public void writeFixedInt(int v) throws IOException {
		out.write(v & 0xFF);
		out.write((v >>> 8) & 0xFF);
		out.write((v >>> 16) & 0xFF);
		out.write((v >>> 24) & 0xFF);
	}

	public void writeFixedLong(long v) throws IOException {
		for (int i = 0; i < 8; i++) out.write((int) (v >>> (8 * i)) & 0xFF);
	}

	public void writeFloat(float v) throws IOException {
		writeFixedInt(Float.floatToIntBits(v));
	}

	public void writeDouble(double v) throws IOException {
		writeFixedLong(Double.doubleToLongBits(v));
	}

	public void writeString(@Nullable String s) throws IOException {
		if (s == null) { writeVarInt(0); return; }
		byte[] b = s.getBytes(StandardCharsets.UTF_8);
		writeVarInt(b.length + 1);
		out.write(b);
	}

	public void writeUUID(@Nullable UUID id) throws IOException {
		if (id == null) { out.write(0); return; }
		out.write(1);
		writeFixedLong(id.getMostSignificantBits());
		writeFixedLong(id.getLeastSignificantBits());
	}

	public void flush() throws IOException {
		out.flush();
	}

	public static byte[] capture(BinaryCodec<?> codec, Object value) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		BinaryWriter w = new BinaryWriter(buf);
		@SuppressWarnings("unchecked")
		BinaryCodec<Object> c = (BinaryCodec<Object>) codec;
		c.write(w, value);
		w.flush();
		return buf.toByteArray();
	}
}