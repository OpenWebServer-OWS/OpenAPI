package com.openwebserver.utils.OpenAPI.Components;

import com.openwebserver.core.WebException;

import java.lang.annotation.Annotation;


public class OpenApiException extends WebException{

    public OpenApiException(String message) {
        super(message);
    }

    public static class NotationException extends OpenApiException {
        public NotationException(String message){
            super(message);
        }
        public NotationException(Class<? extends Annotation> notFound){
            super(notFound.getSimpleName() + " Not found in specification");
        }
    }
}
