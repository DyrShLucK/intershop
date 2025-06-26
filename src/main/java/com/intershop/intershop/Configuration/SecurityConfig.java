package com.intershop.intershop.Configuration;

import com.intershop.intershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.codec.FormHttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    @Autowired
    private ReactiveClientRegistrationRepository clientRegistrationRepository;
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveUserDetailsService userDetailsService
    ) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/intershop", "/intershop/products/**", "/intershop/item/**","/login/**", "/register/**", "/").permitAll()
                        .pathMatchers("/admin/**").hasRole("MANAGER")
                        .anyExchange().authenticated()
                )
                .formLogin(withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((exchange, authentication) -> {
                            ServerWebExchange serverWebExchange = exchange.getExchange();
                            oidcLogoutSuccessHandler();
                            serverWebExchange.getSession()
                                    .flatMap(WebSession::invalidate)
                                    .subscribe();

                            serverWebExchange.getResponse().getCookies().forEach((name, cookies) -> {
                                cookies.forEach(cookie -> {
                                    ResponseCookie deleteCookie = ResponseCookie.from(name, "")
                                            .maxAge(0)
                                            .path(cookie.getPath() != null ? cookie.getPath() : "/")
                                            .domain(cookie.getDomain())
                                            .secure(cookie.isSecure())
                                            .httpOnly(cookie.isHttpOnly())
                                            .sameSite(cookie.getSameSite())
                                            .build();

                                    serverWebExchange.getResponse().addCookie(deleteCookie);
                                });
                            });

                            serverWebExchange.getPrincipal()
                                    .filter(principal -> principal instanceof Authentication)
                                    .map(principal -> (Authentication) principal)
                                    .doOnNext(auth -> auth.setAuthenticated(false))
                                    .subscribe();

                            ServerHttpResponse response = serverWebExchange.getResponse();
                            response.setStatusCode(HttpStatus.FOUND);
                            response.getHeaders().setLocation(URI.create("/login?logout"));

                            return response.setComplete();
                        }).logoutSuccessHandler(oidcLogoutSuccessHandler())
                )
                .exceptionHandling(handling -> handling
                        .accessDeniedHandler((exchange, denied) -> {
                            ServerHttpResponse response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.FOUND);
                            response.getHeaders().setLocation(URI.create("/intershop"));
                            return response.setComplete();
                        })
                )
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(withDefaults())
                )
                .oauth2Login(withDefaults())
                .authenticationManager(new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService))
                .build();
    }
    @Bean
    public ApplicationRunner initUsers(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            userRepository.findByUsername("user")
                    .switchIfEmpty(userRepository.save(
                            new com.intershop.intershop.model.User("user123", encoder.encode("user123"), "USER")
                    ))
                    .subscribe();

            userRepository.findByUsername("manager")
                    .switchIfEmpty(userRepository.save(
                            new com.intershop.intershop.model.User("manager", encoder.encode("manager"), "MANAGER")
                    ))
                    .subscribe();
        };
    }
    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            String cleanUsername = username;

            return userRepository.findByUsername(cleanUsername)
                    .map(user -> toUserDetails(user, username));
        };
    }

    private UserDetails toUserDetails(com.intershop.intershop.model.User user, String originalUsername) {
        return User.builder()
                .username(originalUsername)
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    @Bean
    public FormHttpMessageReader formHttpMessageReader() {
        return new FormHttpMessageReader();
    }
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setPrincipalClaimName("sub");
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtAuthenticationConverter.setPrincipalClaimName("preferred_username");
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
            var roles =jwt.getClaimAsStringList("spring_sec_roles");

            return Stream.concat(authorities.stream(),
                            roles.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .map(GrantedAuthority.class::cast))
                    .toList();
        });
        return jwtAuthenticationConverter;
    }
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oAuth2UserService() {
        var oidcUserService = new OidcUserService();
        return userRequest -> {
            OidcUser oidcUser = oidcUserService.loadUser(userRequest);

            String prefixedUsername = "kc_" + oidcUser.getPreferredUsername();

            List<String> roles = oidcUser.getClaimAsStringList("spring_sec_roles");
            Collection<GrantedAuthority> authorities = Stream.concat(
                    oidcUser.getAuthorities().stream(),
                    roles.stream().map(SimpleGrantedAuthority::new)
            ).collect(Collectors.toList());

            return new DefaultOidcUser(
                    authorities,
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo(),
                    prefixedUsername
            );
        };
    }
    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedServerLogoutSuccessHandler handler =
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);

        handler.setPostLogoutRedirectUri("http://localhost:8080");

        return handler;
    }
    @Bean
    ReactiveOAuth2AuthorizedClientManager auth2AuthorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService
    ) {
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);

        manager.setAuthorizedClientProvider(ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .refreshToken()
                .build()
        );
        return manager;
    }
}