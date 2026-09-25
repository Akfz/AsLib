package v.akfz.aslib.util.af.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Forces a field to be included in AF serialization.
 * <p>
 * The presence of <i>any</i> AF annotation on a class — this one or
 * {@link AfExclude} — switches it into explicit mode: only fields marked
 * {@code @AfInclude} are written. Unannotated fields are skipped, even if
 * they would otherwise be picked up by the legacy "serialize everything" rule.
 * <p>
 * Fields carrying both {@code @AfInclude} and {@link AfExclude} are skipped —
 * exclude wins.
 * <p>
 * In legacy mode (no AF annotations anywhere in the class) this annotation is
 * meaningless, since every eligible field is already included.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AfInclude {
}