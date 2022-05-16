package com.example.user.service;

import com.example.user.entity.UserProfiles;
import com.example.user.exception.GeneralBusinessException;
import com.example.user.repository.UserProfilesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserProfilesService {

    private final UserProfilesRepository userProfilesRepository;

    public UserProfiles getOrNewUserProfiles(String userId) {
        return userProfilesRepository.findByUserId(userId)
                .orElseGet(UserProfiles::new);
    }

    public boolean isRegisteredUser(String userId) {
        return userProfilesRepository.findByUserId(userId).isPresent();
    }

    public void createOrUpdateUserProfiles(UserProfiles userProfiles) {
        userProfilesRepository.save(userProfiles);
    }

    public UserProfiles getUserProfiles(String userId) {
        return userProfilesRepository.findByUserId(userId)
                .orElseThrow(() -> new GeneralBusinessException("keycloak 사용자 정보가 존재하지 않습니다."));
    }
}
