package n.paradox.aslib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Скрывает класс, в dev можно пользоваться всеми методами, а в релизном jar класс становится пустым
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface DontCompile {
}