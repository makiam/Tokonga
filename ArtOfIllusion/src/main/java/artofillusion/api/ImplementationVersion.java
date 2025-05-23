package artofillusion.api;

public @interface ImplementationVersion {
    int min() default 0;
    int current() default 0;
}
