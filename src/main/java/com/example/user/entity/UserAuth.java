package com.example.user.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@SuperBuilder
@Document(collection = "auth")
public class UserAuth {
    private String userId;
    private String password;

    @CreatedDate
    private LocalDateTime created;
    @LastModifiedDate
    private LocalDateTime lastupd;

    public void encryptPassword(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.password = bCryptPasswordEncoder.encode(password);
    }
}
