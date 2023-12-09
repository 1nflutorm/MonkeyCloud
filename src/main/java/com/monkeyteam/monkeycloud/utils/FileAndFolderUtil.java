package com.monkeyteam.monkeycloud.utils;

import com.monkeyteam.monkeycloud.entities.*;
import com.monkeyteam.monkeycloud.repositories.*;
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
    private FavoriteFileRepository favoriteFileRepository;
    private FavoriteFolderRepository favoriteFolderRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setFolderRepository(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @Autowired
    public void setInheritorFoldersRepository(InheritorFoldersRepository inheritorFoldersRepository) {
        this.inheritorFoldersRepository = inheritorFoldersRepository;
    }

    @Autowired
    public void setFavoriteFileRepository(FavoriteFileRepository favoriteFileRepository){
        this.favoriteFileRepository = favoriteFileRepository;
    }

    @Autowired
    public void setFavoriteFolderRepository(FavoriteFolderRepository favoriteFolderRepository){
        this.favoriteFolderRepository = favoriteFolderRepository;
    }

    public Boolean checkFavorite(Item item, String username) {
        Optional<User> optUser = userRepository.findByUsername(username);
        if(!optUser.isPresent())
            return false;

        if (!item.isDir()) {
            Optional<FavoriteFile> optionalFavoriteFile = favoriteFileRepository.findFileByUserIdAndFilePath(optUser.get().getUser_id(), item.objectName());
            if(optionalFavoriteFile.isPresent())
                return true;
        } else {
            Long userId = optUser.get().getUser_id();
            Optional<Folder> optionalFolder = folderRepository.findFolderByUserIdAndPath(userId, item.objectName());
            if(!optionalFolder.isPresent()) {
                return false;
            }
            Optional<FavoriteFolder> optionalFavoriteFolder = favoriteFolderRepository.findByUserIdAndFolderId(userId, optionalFolder.get().getFolderId());
            if(optionalFavoriteFolder.isPresent())
                return true;
        }
        return false;
    }

//    public String[] getCorrectNamesForItem(Item item, String folder) {
//        String objectName = "";
//        int lastSlash = item.objectName().lastIndexOf('/');
//        if (!item.isDir()) {//если не дитректория, то удаляем последний слэш
//            objectName = item.objectName().substring(lastSlash + 1);
//        } else {// если директория, то удаляем предпоследний и послдедний слэш
//            objectName = item.objectName().substring(0, lastSlash);
//            objectName = objectName.substring(objectName.lastIndexOf('/') + 1);
//        }
//        lastSlash = folder.lastIndexOf('/');
//        String folderName = "";
//        if (lastSlash == folder.length() - 1 && lastSlash != -1)
//            folderName = folder.substring(0, lastSlash);
//        return new String[]{objectName, folderName};
//    }

    public String[] getCorrectNamesForItem(Item item) {
        String objectName = "";
        String folderName = item.objectName();
        int lastSlash = item.objectName().lastIndexOf('/');
        if (!item.isDir()) {
            objectName = item.objectName().substring(lastSlash + 1);
            //folderName = item.objectName().substring(0, lastSlash);
        } else {
            objectName = item.objectName().substring(0, lastSlash);
            folderName = objectName;
            objectName = objectName.substring(objectName.lastIndexOf('/') + 1);
            lastSlash = folderName.lastIndexOf('/');
        }
        if (lastSlash == -1) {
            folderName = "";
        } else {
            folderName = folderName.substring(0, lastSlash);
        }
        return new String[]{objectName, folderName};
    }

    public void addDirsToDataBaseFromUploadedFolder(List<SnowballObject> objectList, String username) {
        /*
        1. найти id пользователя
        2. создать папку и сохранить ее в бд
        3. найти родительскую папку
        4. если родительская папка не создана - создать ее
        5. сохранить данные в таблицу "наследование папок"
         */
        for(SnowballObject object : objectList){
            Long userId = userRepository.findByUsername(username).get().getUser_id();
            String path = object.name();//достаем путь к файлу
            int lastSlash = path.lastIndexOf('/');
            String folderPath = path.substring(0, lastSlash + 1);// достаем полный путь до папки
            path = path.substring(0, lastSlash);
            lastSlash = path.lastIndexOf('/');
            String folderName = path.substring(lastSlash + 1);// достаем имя папки

            Optional<Folder> foundFolder = folderRepository.findFolderByUserIdAndPath(userId, folderPath);
            if(foundFolder.isEmpty())
                saveFolder(folderName, folderPath, userId);

            String parentFolderPath = null;
            String childFolderPath = folderPath;
            lastSlash = path.lastIndexOf('/');
            if (lastSlash != -1){
                path = path.substring(0, lastSlash );
                lastSlash = path.lastIndexOf('/');
                folderName = path.substring(lastSlash + 1);

                parentFolderPath = path + "/";
                Folder folderParent = folderRepository.findFolderByUserIdAndPath(userId, parentFolderPath).get();
                Folder folderChild = folderRepository.findFolderByUserIdAndPath(userId, childFolderPath).get();
                if(folderRepository.findFolderByUserIdAndPath(userId, childFolderPath).isEmpty())
                    folderParent = saveFolder(folderName, parentFolderPath, userId);
                InheritorFolder inheritorFolder = new InheritorFolder();
                inheritorFolder.setParentFolderId(folderParent.getFolderId());
                inheritorFolder.setChildFolderId(folderChild.getFolderId());
                inheritorFoldersRepository.save(inheritorFolder);

            }

        }
    }

    private Folder saveFolder(String folderName, String folderPath, Long userId) {
        Folder folder = new Folder();
        folder.setFolderAccess(1);
        folder.setFolderName(folderName);
        folder.setFolderPath(folderPath);
        folder.setUserId(userId);
        return folderRepository.save(folder);
    }

}
