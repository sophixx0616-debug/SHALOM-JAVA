package com.shalom.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomOAuth2UserService oAuth2UserService;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrations;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                          CustomOAuth2UserService oAuth2UserService,
                          ObjectProvider<ClientRegistrationRepository> clientRegistrations) {
        this.successHandler = successHandler;
        this.oAuth2UserService = oAuth2UserService;
        this.clientRegistrations = clientRegistrations;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/login", "/tienda", "/css/**", "/js/**", "/img/**", "/fonts/**",
                        "/auth/**", "/forgot-password", "/password/reset/**").permitAll()
                .requestMatchers("/users/**", "/fidelizacion/**", "/services/create", "/services/*/edit",
                        "/specialists/create", "/specialists/*/edit", "/inventory/**",
                        "/reportes/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("shalom-remember-key")
                .tokenValiditySeconds(30 * 24 * 60 * 60)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        // Google OAuth2 (solo si GOOGLE_CLIENT_ID y GOOGLE_CLIENT_SECRET estan configurados).
        ClientRegistrationRepository repo = clientRegistrations.getIfAvailable();
        if (repo != null) {
            http.oauth2Login(oauth -> oauth
                .loginPage("/login")
                .successHandler(successHandler)
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
            );
        }

        return http.build();
    }
}