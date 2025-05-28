package com.GoPedidos.GoPedidos.Configurações;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;


@Configuration
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfig {

	@Autowired
	SecurityFilter securityFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
				.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/pedido/item-pedido").hasAnyRole("MASTER", "GERENTE")
						.requestMatchers(HttpMethod.PUT, "/pedido/item-pedido/**").hasAnyRole("MASTER", "GERENTE")
						.requestMatchers("/mesas/**").authenticated()
						.requestMatchers("/categorias-produto/**").authenticated()
						.requestMatchers("/auth/**").authenticated()
						.requestMatchers("/colaborador/login").permitAll()
						.requestMatchers("/produtos/criarproduto").hasAnyRole("MASTER", "ADMIN", "GERENTE")
						.requestMatchers("/produtos/**").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
						.requestMatchers("/usuario/**").permitAll()
						.requestMatchers("/auth/criarColaborador").hasRole("MASTER")
						.requestMatchers(HttpMethod.DELETE, "/auth/deletarUsuario").hasRole("MASTER")
						.anyRequest().authenticated())
				.addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// Configuração mais segura para produção
		configuration.setAllowedOrigins(List.of(
				"https://jcmtech.store",
				"http://localhost:4200" ,
				"http://192.168.1.8:4200/"	));

		// Métodos permitidos
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

		// Cabeçalhos permitidos
		configuration.setAllowedHeaders(List.of(
				"Authorization",
				"Content-Type",
				"X-Requested-With",
				"Accept",
				"Origin",
				"Access-Control-Request-Method",
				"Access-Control-Request-Headers"
		));

		// Cabeçalhos expostos
		configuration.setExposedHeaders(List.of(
				"Authorization",
				"Content-Disposition",
				"Access-Control-Allow-Origin",
				"Access-Control-Allow-Credentials"
		));

		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

//configuracao ngnix backend
//
//server {
//	listen 80;
//	server_name backend.jcmtech.store;
//
//    # Redireciona de HTTP para HTTPS
//	return 301 https://$host$request_uri;
//}
//
//server {
//	listen 443 ssl;
//	server_name backend.jcmtech.store;
//
//	ssl_certificate /etc/letsencrypt/live/backend.jcmtech.store/fullchain.pem;
//	ssl_certificate_key /etc/letsencrypt/live/backend.jcmtech.store/privkey.pem;
//	include /etc/letsencrypt/options-ssl-nginx.conf;
//	ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;
//
//    # Proxy para o backend
//	location / {
//			proxy_pass http://localhost:8080;
//	proxy_set_header Host $host;
//	proxy_set_header X-Real-IP $remote_addr;
//	proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
//	proxy_set_header X-Forwarded-Proto $scheme;
//    }
//}
