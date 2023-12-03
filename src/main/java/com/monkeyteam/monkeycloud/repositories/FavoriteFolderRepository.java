package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.FavoriteFolder;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface FavoriteFolderRepository extends CrudRepository<FavoriteFolder, FavoriteFolder> {
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM favorite_folders WHERE user_id = ? and folder_id = ?", nativeQuery = true)
    void deleteFromFavorite(Long userId, Long folderId);

    @Query(value = "SELECT * FROM favorite_folders WHERE user_id = ? and folder_id = ?", nativeQuery = true)
    Optional<FavoriteFolder> findByUserIdAndFolderId(Long userId, Long folderId);
}