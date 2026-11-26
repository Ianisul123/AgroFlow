package com.example.agroflowbackend.repository;

import com.example.agroflowbackend.domain.UserAccount;
import com.example.agroflowbackend.persistence.entity.UserAccountEntity;
import com.example.agroflowbackend.persistence.mapper.UserAccountMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserAccountRepository {

    private final UserAccountJpaRepository userAccountJpaRepository;
    private final UserAccountMapper userAccountMapper;

    public UserAccountRepository(UserAccountJpaRepository userAccountJpaRepository, UserAccountMapper userAccountMapper) {
        this.userAccountJpaRepository = userAccountJpaRepository;
        this.userAccountMapper = userAccountMapper;
    }

    public UserAccount save(UserAccount userAccount) {
        UserAccountEntity entity = userAccountMapper.toEntity(userAccount);
        UserAccountEntity savedEntity = userAccountJpaRepository.save(entity);
        return userAccountMapper.toDomain(savedEntity);
    }

    public Optional<UserAccount> findByEmail(String email) {
        return userAccountJpaRepository.findByEmail(email)
                .map(userAccountMapper::toDomain);
    }

    public Optional<UserAccount> findById(Long id) {
        return userAccountJpaRepository.findById(id)
                .map(userAccountMapper::toDomain);
    }

    public boolean existsByEmail(String email) {
        return userAccountJpaRepository.existsByEmail(email);
    }
}
