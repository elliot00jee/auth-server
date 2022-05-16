package com.example.user.service;

import com.example.user.entity.UserAuth;
import com.example.user.entity.UserProfiles;
import com.example.user.exception.GeneralBusinessException;
import com.example.user.repository.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserAuthService implements UserDetailsService {
    private final UserAuthRepository userAuthRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public void createUser(UserAuth userAuth) {
        if(userAuthRepository.findByUserId(userAuth.getUserId()).isPresent()) {
            throw new GeneralBusinessException("중복된 UserId 입니다.");
        }

        userAuth.encryptPassword(passwordEncoder);
        userAuthRepository.save(userAuth);
    }

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
