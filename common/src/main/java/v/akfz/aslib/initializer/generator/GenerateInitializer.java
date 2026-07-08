package v.akfz.aslib.initializer.generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Чтобы генерировалось, аннотированный класс должен быть в src/sharedMixins или в src/generated, иначе будет игнор
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateInitializer {
    LoaderType loader();
    Class<? extends InitializerClass> mainClass() default NotAClass.class;
    String modId();
    boolean isClient() default false;
    String packageStr() default "null"; //null - сгенерирует
    String addClassNameForge() default "_forge"; // сгенерирует класс как <Имя аннотированного>+addclassname типо AsLib_forge
    String addClassNameFabric() default "_fabric";
}