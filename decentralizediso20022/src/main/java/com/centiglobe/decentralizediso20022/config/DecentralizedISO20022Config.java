package com.centiglobe.decentralizediso20022.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class DecentralizedISO20022Config extends WebMvcConfigurationSupport {
    
    @Override
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new ApiVerRequestMappingHandlerMapping();
    }
}