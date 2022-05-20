package com.example.user.security;

import com.example.user.service.TokenService;
import com.example.user.service.UserProfilesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.Filter;

/**
 * http://localhost:8082/oauth2/authorization/keycloak
 */
@RequiredArgsConstructor
@Configuration
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    private final UserDetailServiceImpl userDetailServiceImpl;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final ExceptionHandlerFilter exceptionHandlerFilter;
    private final VerifyTokenFilter tokenVerifyFilter;

    private final PasswordEncoder passwordEncoder;

    private final UserProfilesService userProfilesService;
    private final TokenService tokenService;

    private final ObjectMapper objectMapper;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.cors().disable();
        http    .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .formLogin().disable()
                .httpBasic().disable()
                .authorizeRequests().antMatchers("/signin", "/**/signup","/login/**", "/tokens/**", "/error").permitAll()
                .anyRequest().authenticated()
            .and()
                .addFilter(customAuthenticationFilter())
                .addFilterBefore(exceptionHandlerFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenVerifyFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login()
                .successHandler(authenticationSuccessHandler);
    }

    @Bean
    public Filter customAuthenticationFilter() throws Exception {
        Filter filter = new CustomAuthenticationFilter(
                authenticationManager(),
                objectMapper,
                tokenService,
                userProfilesService);
        return filter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailServiceImpl)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
