package com.centiglobe.decentralizediso20022.config;

import java.lang.reflect.Method;

import com.centiglobe.decentralizediso20022.annotation.ApiVersion;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Combines the standard request mapping with a request mapping containing URL API
 * version prefixes.
 * 
 * @author William Stackenäs
 */
public class ApiVerRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if(info == null) return null;

        ApiVersion methodAnnotation = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        if(methodAnnotation != null) {
            RequestCondition<?> methodCondition = getCustomMethodCondition(method);
            // Concatenate our ApiVersion with the usual request mapping
            info = createApiVersionInfo(methodAnnotation, methodCondition).combine(info);
        } else {
            ApiVersion typeAnnotation = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
            if(typeAnnotation != null) {
                RequestCondition<?> typeCondition = getCustomTypeCondition(handlerType);
                // Concatenate our ApiVersion with the usual request mapping
                info = createApiVersionInfo(typeAnnotation, typeCondition).combine(info);
            }
        }

        return info;
    }

    private RequestMappingInfo createApiVersionInfo(ApiVersion annotation, RequestCondition<?> customCondition) {
        int[] vers = annotation.value();
        String[] prefixes = new String[vers.length];
        for(int i = 0; i < vers.length; i++) {
            // Build the URL API version prefix
            prefixes[i] = "v" + vers[i]; 
        }

        return RequestMappingInfo
            .paths(prefixes)
            .customCondition(customCondition)
            .build();
    }

}