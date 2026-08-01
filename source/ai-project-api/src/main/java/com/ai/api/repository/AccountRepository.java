package com.ai.api.repository;

import com.ai.api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Account findAccountByUsername(String username);

    Boolean existsByUsername(String username);

    Optional<Account> findAccountByEmail(String email);

    Optional<Account> findAccountByPhone(String phone);

    Boolean existsByPhoneAndStatusNot(String phone, int status);

    Boolean existsByEmailAndStatusNot(String email, int status);

    Optional<Account> findByIdAndStatus(Long id, Integer status);
}
