package com.votingsystem.voting_system.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomAuthSuccessHandler customAuthSuccessHandler;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/home", "/login", "/register", "/register/**", "/about", "/about/**", "/contact", "/contact/**", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                // Public voter listings (read-only)
                .requestMatchers("/voter/events", "/voter/events/**").permitAll()
                .requestMatchers("/voter/winners", "/voter/winners/**").permitAll()
                // Allow voter dashboard access for testing
                .requestMatchers("/voter/dashboard").permitAll()
                .requestMatchers("/voter/test").permitAll()
                .requestMatchers("/voter/profile").permitAll()
                .requestMatchers("/voter/my-votes").permitAll()
                .requestMatchers("/voter/tickets", "/voter/tickets/**").permitAll()
                // Support tickets accessible to SUPPORT and ADMIN roles
                .requestMatchers("/support/tickets", "/support/tickets/**").hasAnyRole("SUPPORT", "ADMIN")
                // Public results - accessible to all authenticated users
                .requestMatchers("/results").authenticated()
                // Role-protected areas
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/nominee/dashboard").permitAll() // Temporarily allow access for testing
                .requestMatchers("/nominee/test").permitAll() // Test endpoint
                .requestMatchers("/nominee/tickets", "/nominee/tickets/**").permitAll() // Allow ticket access for testing
                .requestMatchers("/nominee/**").hasRole("NOMINEE")
                .requestMatchers("/support/**").hasAnyRole("SUPPORT", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customAuthSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
