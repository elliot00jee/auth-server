package com.example.user.security;

import com.example.user.entity.UserAuth;
import com.example.user.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserDetailServiceImpl implements UserDetailsService {
    private final UserAuthRepository userAuthRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserAuth userAuth = userAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(userId));

        return new User(
                userAuth.getUserId(),
                userAuth.getPassword(),
                true,
                true,
                true,
                true,
                // Collection<? extends GrantedAuthority> authorities
                new ArrayList<>()
        );
    }
}
