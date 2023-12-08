package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.ListOfData;
import com.monkeyteam.monkeycloud.dtos.MinioDto;
import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.InheritorFolder;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import com.monkeyteam.monkeycloud.repositories.InheritorFoldersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublicAccessService {
    private FolderRepository folderRepository;

    private InheritorFoldersRepository inheritorFoldersRepository;

    @Autowired
    public void setFolderRepository(FolderRepository folderRepository){
        this.folderRepository = folderRepository;
    }

    @Autowired
    public void setInheritorFoldersRepository(InheritorFoldersRepository inheritorFoldersRepository){
        this.inheritorFoldersRepository = inheritorFoldersRepository;
    }
    public ListOfData getPublicFolders(){
        //у дочерней папки не может быть уровень доступа меньше чем у родительской
        List<Folder> folderList = folderRepository.findAllByFolderAccess(3);
        List<MinioDto> resultList = new ArrayList<>();
        for(Folder folder : folderList){
            if(hasParent(folder.getFolderId())){
                //checkParentFolderAccess;
            } else {
                MinioDto minioDto = new MinioDto();

                //resultList.add()
            }
        }
        return null;
    }

    private Boolean hasParent(Long childId){
        Optional<InheritorFolder> optionalInheritorFolder = inheritorFoldersRepository.getInheritFolderByChildId(childId);
        if(optionalInheritorFolder.isEmpty()){
            return false;
        }
        return true;
    }
}
















