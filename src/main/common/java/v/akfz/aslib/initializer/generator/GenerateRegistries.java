package v.akfz.aslib.initializer.generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate class for holder {@link v.akfz.aslib.annotation.RegisterModule}s
 * <p>
 * Example:
 * <pre>{@code
 * @GenerateRegistries(modId = "example")
 * public class Example {
 *     @RegisterModule(id = "example:testitem")
 *     public static final Item testItem = new AirItem();
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateRegistries {
    String modId();
}