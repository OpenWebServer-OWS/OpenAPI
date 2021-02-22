package com.openwebserver.utils.OpenAPI.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenAPI {

    String value() default "";
    String description() default "";
    String summary() default "";
    String[] tags() default {};
    String operationId() default "#";
}
