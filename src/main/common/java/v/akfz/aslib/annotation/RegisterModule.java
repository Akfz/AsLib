package v.akfz.aslib.annotation;

import v.akfz.aslib.registry.RegistryType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface RegisterModule {
    /** modid, DONT WORK IF YOU WRITE NOT {@link v.akfz.aslib.initializer.generator.GenerateRegistries} modid() + ":id"
     *  like, "aslib:item" and more, but not for commands, if you register command you can set null, write anything,
     *  because commands registers from mixin
     *
     *  @return registerId
     */
    String id();

    /**
     * @return registry target
     */
    RegistryType registry() default RegistryType.AUTO;

    /**
     * ResourceLocation path for custom registries outside BuiltInRegistries.
     * Used when {@link #registry()} is set to {@link RegistryType#CUSTOM}.
     */
    String customRegistry() default "";
}