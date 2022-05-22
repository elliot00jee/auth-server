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
}
