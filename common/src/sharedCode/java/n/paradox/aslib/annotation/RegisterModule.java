package n.paradox.aslib.annotation;

import n.paradox.aslib.registry.RegistryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface RegisterModule {
    //РАБОТАЕТ ЕСЛИ В ОДНОМ ПАПКЕ(т.е если регистратор в sharedCode а аннотированный класс в main, то НЕ СРАБОТАЕТ!)
    String id(); // командам не нужен вообще(так что просто писать айди регистратора)

    RegistryType registry() default RegistryType.AUTO;

    String customRegistry() default "";
}