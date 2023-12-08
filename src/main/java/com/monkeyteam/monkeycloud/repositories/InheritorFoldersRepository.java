package com.monkeyteam.monkeycloud.repositories;

import com.monkeyteam.monkeycloud.entities.InheritorFolder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InheritorFoldersRepository extends CrudRepository <InheritorFolder, InheritorFolder> {
    //Optional<InheritorFolder> findById(InheritorFolder inheritorFolder);

    @Query(value = "SELECT * FROM inheritor_folders WHERE child_folder_id = ?", nativeQuery = true)
    Optional<InheritorFolder> getInheritFolderByChildId(Long childId);
}