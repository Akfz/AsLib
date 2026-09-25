package v.akfz.aslib.util.af.io;

import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.util.af.BinaryException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Low-level byte reader. Mirrors {@link BinaryWriter}.
 */
public final class BinaryReader {
	private final InputStream in;

	public BinaryReader(InputStream in) {
		this.in = in;
	}

	public int readByte() throws IOException {
		int b = in.read();
		if (b < 0) throw new EOFException();
		return b;
	}

	public void readFully(byte[] buf) throws IOException {
		int off = 0;
		while (off < buf.length) {
			int n = in.read(buf, off, buf.length - off);
			if (n < 0) throw new EOFException();
			off += n;
		}
	}

	public boolean readBoolean() throws IOException {
		return readByte() != 0;
	}

	public int readVarInt() throws IOException {
		int result = 0, shift = 0;
		while (true) {
			int b = readByte();
			result |= (b & 0x7F) << shift;
			if ((b & 0x80) == 0) return result;
			shift += 7;
			if (shift >= 35) throw new BinaryException("VarInt too long");
		}
	}

	public long readVarLong() throws IOException {
		long result = 0;
		int shift = 0;
		while (true) {
			int b = readByte();
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) return result;
			shift += 7;
			if (shift >= 70) throw new BinaryException("VarLong too long");
		}
	}

	public int readInt() throws IOException {
		int v = readVarInt();
		return (v >>> 1) ^ -(v & 1);
	}

	public long readLong() throws IOException {
		long v = readVarLong();
		return (v >>> 1) ^ -(v & 1);
	}

	public int readFixedInt() throws IOException {
		int b0 = readByte(), b1 = readByte(), b2 = readByte(), b3 = readByte();
		return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
	}

	public long readFixedLong() throws IOException {
		long v = 0;
		for (int i = 0; i < 8; i++) v |= (long) readByte() << (8 * i);
		return v;
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readFixedInt());
	}

	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readFixedLong());
	}

	@Nullable
	public String readString() throws IOException {
		int len = readVarInt();
		if (len == 0) return null;
		len--;
		byte[] b = new byte[len];
		readFully(b);
		return new String(b, StandardCharsets.UTF_8);
	}

	@Nullable
	public UUID readUUID() throws IOException {
		if (readByte() == 0) return null;
		long msb = readFixedLong();
		long lsb = readFixedLong();
		return new UUID(msb, lsb);
	}
}