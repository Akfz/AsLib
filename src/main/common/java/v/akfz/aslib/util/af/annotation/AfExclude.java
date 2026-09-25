package v.akfz.aslib.util.af.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as excluded from AF serialization.
 * <p>
 * <b>Important:</b> the presence of <i>any</i> AF annotation on a class —
 * either {@link AfInclude} or this one — switches the whole class into
 * explicit mode. In explicit mode only {@code @AfInclude} fields are written,
 * so {@code @AfExclude} alone does not give you blacklist behavior; it just
 * documents "I know about this field and it's intentionally omitted."
 * <p>
 * If you want blacklist semantics (skip a few fields, keep the rest), use
 * {@code transient} and leave the class free of AF annotations.
 * <p>
 * If a field carries both {@code @AfInclude} and {@code @AfExclude}, exclude wins.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AfExclude {
}