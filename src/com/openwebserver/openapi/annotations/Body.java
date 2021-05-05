package com.openwebserver.openapi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {

    enum Type{
        multipart_form_data("multipart/form-data"),
        application_json("application/json"),
        x_www_form_encoded("application/x-www-form-urlencoded"),
        raw("raw");

        private final String pretty;

        Type(String s) {
            this.pretty = s;
        }

        public String getPretty() {
            return pretty;
        }
    }

    Type type() default Type.multipart_form_data;
    boolean required() default false;
    String schemaType() default "object";
    String[] properties();
    String[] types();
    boolean[] requires() default {false};



}
