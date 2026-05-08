package com.board.server.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path uploadPath = Path.of("uploads").toAbsolutePath().normalize();
		registry
			.addResourceHandler("/uploads/**")
			.addResourceLocations(uploadPath.toUri().toString());
	}
}
