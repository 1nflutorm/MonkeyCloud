package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends CrudRepository<Folder, Long> {
    Optional<Folder> findByFolderName(String folderName);

    @Query(value = "SELECT * FROM folders WHERE folder_id = ?", nativeQuery = true)
    Optional<Folder> findFolderByFolderId(Long id);

    @Query(value = "SELECT * FROM folders WHERE user_id = ? and folder_path = ?", nativeQuery = true)
    Optional<Folder> findFolderByUserIdAndPath(Long user_id, String folderPath);

    @Query(value = "SELECT * FROM folders WHERE folder_access = ?", nativeQuery = true)
    List<Folder> findAllByFolderAccess(Integer accessValue);

    @Query(value = "SELECT * FROM folders", nativeQuery = true)
    List<Folder> getAll();
    @Modifying
    @Transactional
    @Query(value = "UPDATE folders SET folder_path = ?, folder_name = ? WHERE folder_id = ?", nativeQuery = true)
    void renameFolder(String folderPath, String folderName, Long folderId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE folders SET folder_access = ? WHERE folder_id = ?", nativeQuery = true)
    void setFolderAccess(int folderAccess, Long folderId) throws RuntimeException;

    @Modifying
    @Transactional
    @Query(value = "UPDATE folders SET folder_path = ?, folder_name = ? WHERE folder_id = ?", nativeQuery = true)
    void renameInFolders(String fullPath, String folderName, Long folderId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM folders WHERE folder_id = ?", nativeQuery = true)
    void deleteFolderById(Long folderId);
}
