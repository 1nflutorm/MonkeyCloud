package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.RefreshToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query(value = "delete from refresh_tokens where user_id = ?", nativeQuery = true)
    public void deleteByUserId(long id);
}
