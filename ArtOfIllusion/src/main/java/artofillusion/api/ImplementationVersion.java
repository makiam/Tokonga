package artofillusion.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ImplementationVersion {
    int min() default 0;
    int current() default 0;
}
