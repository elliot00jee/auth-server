package com.example.user.entity;

import com.example.user.exception.GeneralBusinessException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UserProfiles {
    @Id
    private String _id;
    private String userId;
    private String username;
    private String department;
    // [TODO: enum 으로 바꾸기]
    private String role;

    @CreatedDate
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public void setUserProfilesBy(UserProfiles updatedUserProfiles) {
        if(this.userId == null) {
            this.userId = updatedUserProfiles.getUserId();
        }

        this.username = updatedUserProfiles.username;
        this.department = updatedUserProfiles.department;
    }

    public void setRole(String role) {
        if(this.role != null && !this.role.isEmpty()) {
            throw new GeneralBusinessException("role은 변경될 수 없습니다. role: " + this.role);
        }
        this.role = role;
    }
}
