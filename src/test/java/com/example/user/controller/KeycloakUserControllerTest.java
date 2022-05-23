package com.example.user.controller;

import com.example.user.controller.dto.SignupDto;
import com.example.user.entity.UserProfiles;
import com.example.user.util.ResponseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("local")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class KeycloakUserControllerTest {
    public static final String TEST_USER_01 = "testUser01";
    private static final String TEST_USER_02 = "testUser02";
    private static final String TEST_USER_03 = "testUser03";
    public static final String COL_USER_PROFILES = "userProfiles";
    public static final String ROLE_ADMIN = "Admin";
    public static final String MONGODB_CONN_STR = "mongodb://chatbot:chatbot@localhost:27017/chatbot";

    @Autowired
    MockMvc mockMvc;
    static MongoTemplate mongoTemplate;

    final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() {
        mongoTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(MONGODB_CONN_STR));
        mongoTemplate.insert(UserProfiles.builder().userId(KeycloakUserControllerTest.TEST_USER_01).build());
        mongoTemplate.insert(UserProfiles.builder().userId(KeycloakUserControllerTest.TEST_USER_02).role(ROLE_ADMIN).build());
    }

    @AfterAll
    static void tearDown() {
        mongoTemplate.remove(new Query(new Criteria("userId").is(TEST_USER_01)), COL_USER_PROFILES);
        mongoTemplate.remove(new Query(new Criteria("userId").is(TEST_USER_02)), COL_USER_PROFILES);
    }

    @Nested
    @DisplayName("키클락 사용자 회원가입 테스트")
    class createKeycloakUser {
        @Test
        @DisplayName("성공 테스트(role이 없는 사용자)")
        void role_isempty_user() throws Exception {

            mockMvc.perform(post("/keycloak/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    SignupDto.builder().userId(TEST_USER_01).role(ROLE_ADMIN).build())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(handler().handlerType(KeycloakUserController.class))
                    .andExpect(handler().methodName("createKeycloakUser"))
                    .andExpect(jsonPath("$.resultCd", is(ResponseUtils.ResultCd.S.name())))
                    .andExpect(jsonPath("$.data.accesstoken").exists())
                    .andExpect(cookie().exists("refreshtoken"))
                    .andExpect(cookie().httpOnly("refreshtoken", true));

        }

        @Test
        @DisplayName("실패 테스트(role이 이미 등록된 사용자)")
        void already_existed_user() throws Exception {
            mockMvc.perform(post("/keycloak/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    SignupDto.builder().userId(TEST_USER_02).role("Admin").build())))
                    .andDo(print())
                    .andExpect(status().is4xxClientError())
                    .andExpect(handler().handlerType(KeycloakUserController.class))
                    .andExpect(handler().methodName("createKeycloakUser"))
                    .andExpect(jsonPath("$.resultCd", is(ResponseUtils.ResultCd.BE.name())))
                    .andExpect(jsonPath("$.resultMessage", is("이미 등록된 사용자입니다.")));
        }

        @Test
        @DisplayName("실패 테스트(키클락 로그인 시 등록된 데이터가 없는 사용자)")
        void not_exist_keycloak_login_history() throws Exception {
            mockMvc.perform(post("/keycloak/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    SignupDto.builder().userId(TEST_USER_03).role("Admin").build())))
                    .andDo(print())
                    .andExpect(status().is4xxClientError())
                    .andExpect(handler().handlerType(KeycloakUserController.class))
                    .andExpect(handler().methodName("createKeycloakUser"))
                    .andExpect(jsonPath("$.resultCd", is(ResponseUtils.ResultCd.BE.name())))
                    .andExpect(jsonPath("$.resultMessage", is("keycloak으로 로그인한 사용자 정보가 존재하지 않습니다.")));
        }
    }
}