package com.intershop.intershop.service;

import com.intershop.intershop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//public class CustomUserDetails implements UserDetailsService {
//    private final String username;
//    private final String source;
//    private final Collection<? extends GrantedAuthority> authorities;
//
//    public CustomUserDetails(String username, String source, Collection<? extends GrantedAuthority> authorities) {
//        this.username = username;
//        this.source = source;
//        this.authorities = authorities;
//    }
//
//    public String getUniqueIdentifier() {
//        return source + "_" + username;
//    }
//
//}

