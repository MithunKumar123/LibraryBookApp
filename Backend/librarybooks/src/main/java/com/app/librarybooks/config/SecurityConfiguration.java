package com.app.librarybooks.config;

import com.app.librarybooks.utils.ExtractJWT;
import com.okta.spring.boot.oauth.Okta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfiguration {

    private String[] authorizedRequests = {
            "/api/books/**",
            "/api/books/secure/**",
            "/api/reviews/secure/**",
            "/api/messages/secure/**",
            "/api/admin/secure/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf(csrf -> csrf.disable());

        http
                .csrf(csrf -> csrf.disable())
                        .authorizeHttpRequests( auth -> auth
                                .requestMatchers("/api/books/secure/**").authenticated()
                                .requestMatchers("/api/reviews/secure/**").authenticated()
                                .requestMatchers("/api/messages/secure/**").authenticated()
                                .requestMatchers("/api/admin/secure/**").authenticated()
                                .requestMatchers("/api/**").permitAll())
                                .oauth2ResourceServer(configure -> configure.jwt(Customizer.withDefaults()));


        http.cors(
                cors -> cors.configurationSource(corsConfigurationSource())
        );

        http.setSharedObject(ContentNegotiationStrategy.class,
                new HeaderContentNegotiationStrategy());

        Okta.configureResourceServer401ResponseBody(http);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("https://localhost:3000"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST","PUT","DELETE"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

}
