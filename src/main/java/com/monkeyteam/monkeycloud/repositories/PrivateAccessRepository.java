package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.PrivateAccessEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivateAccessRepository extends CrudRepository<PrivateAccessEntity, PrivateAccessEntity> {

}
