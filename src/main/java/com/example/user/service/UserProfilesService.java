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

    public void createOrUpdateUserProfiles(UserProfiles userProfiles) {
        userProfilesRepository.save(userProfiles);
    }
    
    public Optional<UserProfiles> getUserProfilesOptional(String userId) {
        return userProfilesRepository.findByUserId(userId);
    }

    public UserProfiles createKeycloakUser(UserProfiles userProfiles) {
        UserProfiles savedUserProfiles = getUserProfilesOptional(userProfiles.getUserId())
                .orElseThrow(() -> new GeneralBusinessException("keycloak으로 로그인한 사용자 정보가 존재하지 않습니다."));

        try {
            savedUserProfiles.setRole(userProfiles.getRole());
        }catch (GeneralBusinessException e) {
            throw new GeneralBusinessException("이미 등록된 사용자입니다.");
        }

        createOrUpdateUserProfiles(savedUserProfiles);

        return savedUserProfiles;
    }
}
