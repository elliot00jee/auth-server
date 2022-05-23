package com.example.user.controller;

import com.example.user.controller.dto.LoginDto;
import com.example.user.entity.UserAuth;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RegularUserControllerTest {
    private static final String MONGODB_CONN_STR = "mongodb://chatbot:chatbot@localhost:27017/chatbot";
    private static final String ROLE_ADMIN = "Admin";
    private static final String TEST_USER_01 = "testUser01";
    private static final String TEST_USER_02 = "testUser02";
    private static final String COL_USER_PROFILES = "userProfiles";
    private static final String COL_USER_AUTH = "auth";
    static MongoTemplate mongoTemplate;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @BeforeAll
    static void setUp() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        mongoTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(MONGODB_CONN_STR));

        mongoTemplate.insert(
                UserProfiles.builder().userId(TEST_USER_01).role(ROLE_ADMIN).build());
        mongoTemplate.insert(
                UserAuth.builder().userId(TEST_USER_01).password(bCryptPasswordEncoder.encode(TEST_USER_01)).build()
        );
        mongoTemplate.insert(
                UserAuth.builder().userId(TEST_USER_02).password(bCryptPasswordEncoder.encode(TEST_USER_02)).build()
        );
    }

    @AfterAll
    static void tearDown() {
        mongoTemplate.remove(new Query(new Criteria("userId").is(TEST_USER_01)), COL_USER_PROFILES);
        mongoTemplate.remove(new Query(new Criteria("userId").is(TEST_USER_01)), COL_USER_AUTH);
        mongoTemplate.remove(new Query(new Criteria("userId").is(TEST_USER_02)), COL_USER_AUTH);
    }

    @Nested
    @DisplayName("일반 사용자 로그인 테스트")
    class signin {
        @Test
        @DisplayName("성공 테스트")
        void success() throws Exception {
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    LoginDto.builder().userId(TEST_USER_01).password(TEST_USER_01).build())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(handler().handlerType(RegularUserController.class))
                    .andExpect(handler().methodName("signin"))
                    .andExpect(jsonPath("$.resultCd", is(ResponseUtils.ResultCd.S.name())))
                    .andExpect(jsonPath("$.data.accesstoken").exists())
                    .andExpect(cookie().exists("refreshtoken"))
                    .andExpect(cookie().httpOnly("refreshtoken", true));
        }

        @Test
        @DisplayName("실패 테스트(인증 정보 틀림)")
        void authentication_exception() throws Exception {
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    LoginDto.builder().userId(TEST_USER_01).password("failpassword").build())))
                    .andDo(print())
                    .andExpect(status().is4xxClientError())
                    .andExpect(handler().handlerType(RegularUserController.class))
                    .andExpect(handler().methodName("signin"))
                    .andExpect(jsonPath("$.resultCd", is(ResponseUtils.ResultCd.BE.name())))
                    .andExpect(jsonPath("$.resultMessage", is("아이디 또는 패스워드가 유효하지 않습니다.")));
        }

        @Test
        @DisplayName("실패 테스트(사용자 프로필 정보 없음)")
        void userprofile_not_exist() throws Exception {
            mockMvc.perform(post("/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    LoginDto.builder().userId(TEST_USER_02).password(TEST_USER_02).build())))
                    .andDo(print())
                    .andExpect(status().is4xxClientError())
                    .andExpect(handler().handlerType(RegularUserController.class))
                    .andExpect(handler().methodName("signin"))
                    .andExpect(jsonPath("$.resultCd", is(ResponseUtils.ResultCd.BE.name())))
                    .andExpect(jsonPath("$.resultMessage", is("사용자 프로필 정보가 존재하지 않습니다.")));
        }
    }
}