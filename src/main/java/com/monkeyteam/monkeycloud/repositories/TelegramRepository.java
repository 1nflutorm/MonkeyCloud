package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.TelegramUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TelegramRepository extends CrudRepository <TelegramUser, Long> {
    @Query(value = "SELECT * FROM telegram WHERE user_id = ?", nativeQuery = true)
    Optional<TelegramUser> findByUserId(Long id);
}
