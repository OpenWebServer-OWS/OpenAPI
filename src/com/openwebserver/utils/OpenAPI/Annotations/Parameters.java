package com.openwebserver.utils.OpenAPI.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameters {

    String[] names();
    String[] in();
    String[] descriptions();
    boolean[] required() default {false};
    String type() default "String";
    String format() default "";

}
