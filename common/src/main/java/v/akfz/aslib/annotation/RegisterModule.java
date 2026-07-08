package v.akfz.aslib.annotation;

import v.akfz.aslib.registry.RegistryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface RegisterModule {
    String id(); // командам не нужен вообще(так что просто писать айди регистратора)

    RegistryType registry() default RegistryType.AUTO;

    String customRegistry() default "";
}