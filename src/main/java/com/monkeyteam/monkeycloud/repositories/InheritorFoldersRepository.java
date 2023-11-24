package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.InheritorFolder;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InheritorFoldersRepository extends CrudRepository <InheritorFolder, InheritorFolder> {
    //Optional<InheritorFolder> findByIds(String parentId, String childId);
}
