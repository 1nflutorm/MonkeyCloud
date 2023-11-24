package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface FolderRepository extends CrudRepository<Folder, Long> {
    Optional<Folder> findByFolderName(String folderName);

    @Query(value = "SELECT * FROM folders WHERE folder_path = ?", nativeQuery = true)
    Optional<Folder> getFolder(String folderPath);

}
