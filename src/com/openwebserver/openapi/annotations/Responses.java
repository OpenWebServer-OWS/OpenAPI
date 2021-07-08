package com.openwebserver.openapi.annotations;

import com.openwebserver.core.http.content.Code;

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
