package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users set session = true where username = ?", nativeQuery = true)
    void setSessionActive(String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users set session = false where username = ?", nativeQuery = true)
    void setSessionInactive(String username);

}
