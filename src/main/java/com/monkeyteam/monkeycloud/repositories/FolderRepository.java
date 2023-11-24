package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FolderRepository extends CrudRepository<Folder, Long> {
    Optional<Folder> findByFolderName(String folderName);

    @Query(value = "SELECT * FROM folders WHERE user_id = ? and folder_path = ?", nativeQuery = true)
    public Optional<Folder> findFolderByUserIdAndPath(Long userId, String path);
}
