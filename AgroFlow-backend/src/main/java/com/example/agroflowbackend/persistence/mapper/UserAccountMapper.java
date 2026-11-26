package com.example.agroflowbackend.persistence.mapper;

import com.example.agroflowbackend.domain.UserAccount;
import com.example.agroflowbackend.persistence.entity.UserAccountEntity;
import org.springframework.stereotype.Component;

@Component
public class UserAccountMapper {

    public UserAccountEntity toEntity(UserAccount userAccount) {
        if (userAccount == null) {
            return null;
        }

        return new UserAccountEntity(
                userAccount.getId(),
                userAccount.getName(),
                userAccount.getEmail(),
                userAccount.getPasswordHash(),
                userAccount.getRole()
        );
    }

    public UserAccount toDomain(UserAccountEntity entity) {
        if (entity == null) {
            return null;
        }

        return new UserAccount(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole()
        );
    }
}
