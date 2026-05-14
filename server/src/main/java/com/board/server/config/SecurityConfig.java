package com.board.server.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.board.server.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
		http
			.csrf((csrf) -> csrf.disable())
			.cors(Customizer.withDefaults())
			.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/uploads/images/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/uploads/files/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
				.requestMatchers("/api/members/signup", "/api/members/login", "/api/members/refresh", "/api/members/logout").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
				.requestMatchers(HttpMethod.POST, "/api/posts/*/comments").authenticated()
				.requestMatchers(HttpMethod.POST, "/api/uploads/images").authenticated()
				.requestMatchers(HttpMethod.POST, "/api/uploads/files").authenticated()
				.requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
				.requestMatchers(HttpMethod.PUT, "/api/comments/**").authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/uploads/images/**").authenticated()
				.requestMatchers(HttpMethod.DELETE, "/api/uploads/files/**").authenticated()
				.anyRequest().authenticated()
			)
			.exceptionHandling((exceptions) -> exceptions
				.authenticationEntryPoint((request, response, exception) ->
					writeErrorResponse(objectMapper, request, response, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."))
				.accessDeniedHandler((request, response, exception) ->
					writeErrorResponse(objectMapper, request, response, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."))
			)
			.oauth2ResourceServer((oauth2) -> oauth2
				.jwt(Customizer.withDefaults())
				.authenticationEntryPoint((request, response, exception) ->
					writeErrorResponse(objectMapper, request, response, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."))
			);

		return http.build();
	}

	private void writeErrorResponse(
		ObjectMapper objectMapper,
		HttpServletRequest request,
		HttpServletResponse response,
		HttpStatus status,
		String message
	) throws IOException {
		response.setStatus(status.value());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(
			response.getWriter(),
			ErrorResponse.of(status, status.getReasonPhrase(), message, request)
		);
	}

	@Bean
	public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
	}

	@Bean
	public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
		return NimbusJwtDecoder
			.withSecretKey(jwtSecretKey)
			.macAlgorithm(MacAlgorithm.HS256)
			.build();
	}

	@Bean
	public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
		return new SecretKeySpec(
			jwtProperties.secret().getBytes(StandardCharsets.UTF_8),
			"HmacSHA256"
		);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(
			"http://localhost:3000",
			"http://localhost:3001",
			"http://192.168.48.128:3000",
			"http://101.79.23.5:3000"
			));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
