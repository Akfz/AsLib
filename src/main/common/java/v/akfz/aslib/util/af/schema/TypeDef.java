package v.akfz.aslib.util.af.schema;

import java.util.Arrays;
import java.util.Objects;

/**
 * One entry in the schema table. Field references are indices into the owning
 * {@link Schema}'s {@code types} array, which allows recursive types
 * ({@code Node.next -> Node}) and deduplication.
 */
public final class TypeDef {

	public static final byte K_BOOLEAN = 0x01;
	public static final byte K_BYTE    = 0x02;
	public static final byte K_SHORT   = 0x03;
	public static final byte K_INT     = 0x04;
	public static final byte K_LONG    = 0x05;
	public static final byte K_FLOAT   = 0x06;
	public static final byte K_DOUBLE  = 0x07;
	public static final byte K_CHAR    = 0x08;

	public static final byte K_BOOLEAN_BOX = 0x11;
	public static final byte K_BYTE_BOX    = 0x12;
	public static final byte K_SHORT_BOX   = 0x13;
	public static final byte K_INT_BOX     = 0x14;
	public static final byte K_LONG_BOX    = 0x15;
	public static final byte K_FLOAT_BOX   = 0x16;
	public static final byte K_DOUBLE_BOX  = 0x17;
	public static final byte K_CHAR_BOX    = 0x18;

	public static final byte K_STRING     = 0x09;
	public static final byte K_UUID       = 0x0A;
	public static final byte K_BYTES      = 0x0B;
	public static final byte K_ENUM       = 0x10;
	public static final byte K_ARRAY      = 0x20;
	public static final byte K_COLLECTION = 0x21;
	public static final byte K_MAP        = 0x22;
	public static final byte K_OBJECT     = 0x30;
	public static final byte K_CUSTOM     = 0x40;

	public final byte kind;

	public final String className;       // ENUM, COLLECTION, MAP, OBJECT, CUSTOM(value class)
	public final int elemIdx;            // ARRAY, COLLECTION
	public final int keyIdx;             // MAP
	public final int valIdx;             // MAP
	public final String[] fieldNames;    // OBJECT
	public final int[] fieldTypeIdx;     // OBJECT
	public final String[] enumConstants; // ENUM
	public final String codecClass;      // CUSTOM
	public final int codecVersion;       // CUSTOM

	private TypeDef(byte kind, String className, int elemIdx, int keyIdx, int valIdx,
	                String[] fieldNames, int[] fieldTypeIdx, String[] enumConstants,
	                String codecClass, int codecVersion) {
		this.kind = kind;
		this.className = className;
		this.elemIdx = elemIdx;
		this.keyIdx = keyIdx;
		this.valIdx = valIdx;
		this.fieldNames = fieldNames;
		this.fieldTypeIdx = fieldTypeIdx;
		this.enumConstants = enumConstants;
		this.codecClass = codecClass;
		this.codecVersion = codecVersion;
	}

	public static TypeDef primitive(byte kind) {
		return new TypeDef(kind, null, -1, -1, -1, null, null, null, null, 0);
	}

	public static TypeDef enumType(String className, String[] constants) {
		return new TypeDef(K_ENUM, className, -1, -1, -1, null, null, constants, null, 0);
	}

	public static TypeDef array(int elemIdx) {
		return new TypeDef(K_ARRAY, null, elemIdx, -1, -1, null, null, null, null, 0);
	}

	public static TypeDef collection(String implClass, int elemIdx) {
		return new TypeDef(K_COLLECTION, implClass, elemIdx, -1, -1, null, null, null, null, 0);
	}

	public static TypeDef map(String implClass, int keyIdx, int valIdx) {
		return new TypeDef(K_MAP, implClass, -1, keyIdx, valIdx, null, null, null, null, 0);
	}

	public static TypeDef object(String className, String[] fieldNames, int[] fieldTypeIdx) {
		return new TypeDef(K_OBJECT, className, -1, -1, -1, fieldNames, fieldTypeIdx, null, null, 0);
	}

	public static TypeDef custom(String codecClass, String valueClass, int version) {
		return new TypeDef(K_CUSTOM, valueClass, -1, -1, -1, null, null, null, codecClass, version);
	}

	public boolean isUnboxedPrimitive() {
		return kind >= K_BOOLEAN && kind <= K_CHAR;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TypeDef t)) return false;
		return kind == t.kind
				&& elemIdx == t.elemIdx
				&& keyIdx == t.keyIdx
				&& valIdx == t.valIdx
				&& codecVersion == t.codecVersion
				&& Objects.equals(className, t.className)
				&& Arrays.equals(fieldNames, t.fieldNames)
				&& Arrays.equals(fieldTypeIdx, t.fieldTypeIdx)
				&& Arrays.equals(enumConstants, t.enumConstants)
				&& Objects.equals(codecClass, t.codecClass);
	}

	@Override
	public int hashCode() {
		int h = kind;
		h = 31 * h + Objects.hashCode(className);
		h = 31 * h + elemIdx;
		h = 31 * h + keyIdx;
		h = 31 * h + valIdx;
		h = 31 * h + Arrays.hashCode(fieldNames);
		h = 31 * h + Arrays.hashCode(fieldTypeIdx);
		h = 31 * h + Arrays.hashCode(enumConstants);
		h = 31 * h + Objects.hashCode(codecClass);
		h = 31 * h + codecVersion;
		return h;
	}
}