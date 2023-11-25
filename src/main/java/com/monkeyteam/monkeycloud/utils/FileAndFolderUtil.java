package com.monkeyteam.monkeycloud.utils;

import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.InheritorFolder;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import com.monkeyteam.monkeycloud.repositories.InheritorFoldersRepository;
import com.monkeyteam.monkeycloud.repositories.UserRepository;
import io.minio.SnowballObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FileAndFolderUtil {

    private UserRepository userRepository;
    private FolderRepository folderRepository;
    private InheritorFoldersRepository inheritorFoldersRepository;
    @Autowired
    public void setUserRepository(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Autowired
    public void setFolderRepository(FolderRepository folderRepository){
        this.folderRepository = folderRepository;
    }

    @Autowired
    public void setInheritorFoldersRepository(InheritorFoldersRepository inheritorFoldersRepository){
        this.inheritorFoldersRepository = inheritorFoldersRepository;
    }

    public String[] getCorrectNamesForItem(Item item, String folder){
        String objectName = "";
        int lastSlash = item.objectName().lastIndexOf('/');
        if(!item.isDir()) {//если не дитректория, то удаляем последний слэш
            objectName = item.objectName().substring(lastSlash + 1);
        } else {// если директория, то удаляем предпоследний и послдедний слэш
            objectName = item.objectName().substring(0, lastSlash);
            objectName = objectName.substring(objectName.lastIndexOf('/') + 1);
        }
        lastSlash = folder.lastIndexOf('/');
        String folderName = "";
        if(lastSlash == folder.length() - 1 && lastSlash != -1)
            folderName = folder.substring(0, lastSlash);
        return new String[]{objectName, folderName};
    }

    public void addDirsToDataBaseFromUploadedFolder (List<SnowballObject> objectList, String username){
        /*
        1. найти id пользователя
        2. создать папку и сохранить ее в бд
        3. найти родительскую папку
        4. если родительская папка не создана - создать ее
        5. сохранить данные в таблицу "наследование папок"
         */
        objectList.forEach(object-> {
            Long userId = userRepository.findByUsername(username).get().getUser_id();
            String path = object.filename();
            if(!path.endsWith("/"))
                return;

            path = path.substring(0, path.length() - 1);
            Folder folder = saveFolder(object.name(), path, userId);//загружаемая папка
            Long folderId = folder.getFolderId();

            int lastSlash = path.lastIndexOf('/');
            if(lastSlash == -1)
                return;

            String parentFolderPath = path.substring(0, lastSlash);

            Optional<Folder> parentFolder = folderRepository.findFolderByUserIdAndPath(userId, parentFolderPath);
            Long parentFolderId;
            if(!parentFolder.isPresent()) {
                String folderName = path.substring(parentFolderPath.lastIndexOf('/') + 1);
                Folder newFolder = saveFolder(folderName, parentFolderPath, userId);//родительская папка
                parentFolderId = newFolder.getFolderId();
            } else {
                parentFolderId = parentFolder.get().getFolderId();
            }
            InheritorFolder inheritorFolder = new InheritorFolder();
            inheritorFolder.setParentFolderId(parentFolderId);
            inheritorFolder.setChildFolderId(folderId);
            inheritorFoldersRepository.save(inheritorFolder);
        });
    }

    private Folder saveFolder(String folderName, String folderPath, Long userId){
        Folder folder = new Folder();
        folder.setFolderAccess(1);
        folder.setFolderName(folderName);
        folder.setFolderPath(folderPath);
        folder.setUserId(userId);
        return folderRepository.save(folder);
    }

}
