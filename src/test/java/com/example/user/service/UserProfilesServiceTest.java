package com.example.user.service;

import com.example.user.entity.UserProfiles;
import com.example.user.exception.GeneralBusinessException;
import com.example.user.repository.UserProfilesRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfilesServiceTest {

    private static final String TEST_USER_01 = "testUser01";
    private static final String ROLE_ADMIN = "Admin";

    @InjectMocks
    UserProfilesService userProfilesService;

    @Mock
    UserProfilesRepository userProfilesRepository;

    @BeforeEach
    void setUp() {
        if(userProfilesService == null) {
            userProfilesService = new UserProfilesService(userProfilesRepository);
        }
    }

    @Nested
    class CreateKeycloakUser {
        @Test
        @DisplayName("실패 테스트(UserProfiles에 사용자 정보가 없을 경우)")
        void user_profiles_not_exist() {

            when(userProfilesRepository.findByUserId(TEST_USER_01)).thenReturn(Optional.empty());

            UserProfiles userProfiles = UserProfiles.builder().userId(TEST_USER_01).build();
            RuntimeException exception = Assertions.assertThrows(GeneralBusinessException.class,
                    () -> userProfilesService.createKeycloakUser(userProfiles));

            assertThat(exception.getMessage()).isEqualTo("keycloak으로 로그인한 사용자 정보가 존재하지 않습니다.");
        }

        @Test
        @DisplayName("실패 테스트(UserProfiles에 이미 등록된 role이 있을 경우")
        void already_exist_user() {
            UserProfiles userProfiles = UserProfiles.builder().userId(TEST_USER_01).build();
            UserProfiles savedUserProfiles = UserProfiles.builder().userId(TEST_USER_01).role(ROLE_ADMIN).build();
            when(userProfilesRepository.findByUserId(TEST_USER_01)).thenReturn(Optional.of(savedUserProfiles));

            RuntimeException exception = Assertions.assertThrows(GeneralBusinessException.class,
                    () -> userProfilesService.createKeycloakUser(userProfiles));

            assertThat(exception.getMessage()).isEqualTo("이미 등록된 사용자입니다.");
        }
    }
}