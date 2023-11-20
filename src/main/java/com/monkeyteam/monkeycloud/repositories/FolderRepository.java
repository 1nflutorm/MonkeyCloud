package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FolderRepository extends CrudRepository<Folder, Long> {
    Optional<Folder> findByFolderName(String folderName);
}
