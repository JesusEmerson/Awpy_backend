package com.awpy.awpy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${awpy.upload.dir:uploads}")
    private String diretorioUploads;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String localizacao = "file:" + Path.of(diretorioUploads).toAbsolutePath().normalize() + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(localizacao);
    }
}
