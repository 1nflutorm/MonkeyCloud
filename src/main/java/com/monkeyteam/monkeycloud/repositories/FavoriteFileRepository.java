package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.FavoriteFile;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface FavoriteFileRepository extends CrudRepository<FavoriteFile, FavoriteFile> {
    //Optional<User> findByUserId(Long userID);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM favorite_files WHERE user_id = ? and file_path = ?", nativeQuery = true)
    public void deleteFromFavorite(Long id, String filePath);

    @Query (value = "SELECT * FROM favorite_files WHERE user_id = ? and file_path = ?", nativeQuery = true)
    public Optional<FavoriteFile> findFileByUserIdAndFilePath(Long id, String filePath);

    @Transactional
    @Modifying
    @Query(value = "UPDATE favorite_files SET file_path = ? WHERE user_id = ? AND folder_id = ? AND file_path = ?", nativeQuery = true)
    void renameInFavoriteFiles(String newName, Long userId, Long folderId, String oldName);
}
