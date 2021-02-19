package com.openwebserver.utils.OpenAPI.Annotations;

import com.openwebserver.core.Content.Code;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Responses {
    Code[] codes() default {Code.Ok};
    String[] descriptions() default {"No description provided"};
}
