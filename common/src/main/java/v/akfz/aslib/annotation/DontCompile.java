package v.akfz.aslib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//ЕСЛИ ДОБАВЛЕН В build.gradle, то не компилирует аннотированные классы
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface DontCompile {
}