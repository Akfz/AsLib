package v.akfz.aslib.util.af;

/**
 * Thrown when the file's format version is not readable by this build.
 * Subclass of {@link BinaryException} so existing catch blocks keep working.
 * {@code readOrNull} / {@code readOrDefault} swallow exactly this type.
 */
public class IncompatibleFormatException extends BinaryException {
	public IncompatibleFormatException(String message) { super(message); }
	public IncompatibleFormatException(String message, Throwable cause) { super(message, cause); }
}