package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.ListOfData;
import com.monkeyteam.monkeycloud.dtos.MinioDto;
import com.monkeyteam.monkeycloud.dtos.PublicAccessDto;
import com.monkeyteam.monkeycloud.dtos.fileDtos.GetFilesRequest;
import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.InheritorFolder;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import com.monkeyteam.monkeycloud.repositories.InheritorFoldersRepository;
import com.monkeyteam.monkeycloud.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublicAccessService {
    private FolderRepository folderRepository;

    private InheritorFoldersRepository inheritorFoldersRepository;

    private UserRepository userRepository;

    private FileService fileService;

    @Autowired
    public void setFileService (FileService fileService){
        this.fileService = fileService;
    }

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
    public ListOfData getPublicFolders(){
        //у дочерней папки не может быть уровень доступа меньше чем у родительской
        List<Folder> folderList = folderRepository.findAllByFolderAccess(3);
        List<MinioDto> resultList = new ArrayList<>();
        for(Folder folder : folderList){
            Long parentFolderId = getParent(folder.getFolderId());
            if(parentFolderId > -1) {
                int parentFolderAccess = getParentFolderAccess(parentFolderId);
                if (parentFolderAccess == 3)
                    continue;
            }
            MinioDto minioDto = new MinioDto();
            String username = userRepository.findById(folder.getUserId()).get().getUsername();
            minioDto.setUsername(username);
            minioDto.setFolderId(folder.getFolderId());
            minioDto.setIsDir(true);
            minioDto.setPath(folder.getFolderPath());
            minioDto.setName(folder.getFolderName());
            minioDto.setBreadCrums(null);
            minioDto.setSize(null);
            minioDto.setIsFavorite(null);
            minioDto.setDate(null);
            resultList.add(minioDto);
        }
        return new ListOfData(resultList);
    }

    public Long getParent(Long childId){
        Optional<InheritorFolder> optionalInheritorFolder = inheritorFoldersRepository.getInheritFolderByChildId(childId);
        if(optionalInheritorFolder.isEmpty()){
            return -1L;
        }
        return optionalInheritorFolder.get().getParentFolderId();
    }

    private int getParentFolderAccess(Long parentId) {
        Optional<Folder> folder = folderRepository.findFolderByFolderId(parentId);
        if(folder.isEmpty()){
            return -1;
        }
        return folder.get().getFolderAccess();
    }

    public ResponseEntity<?> openFolder(Long folderId){
        try {
            folderRepository.setFolderAccess(3, folderId);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("Доступ открыт");
    }

    public ResponseEntity<?> getFilesInPublicFolder(Long folderId, int folderAccess){
        //username
        //folder
        Folder folder = folderRepository.findFolderByFolderId(folderId).get();
        String folderPath = folder.getFolderPath();
        String username = userRepository.findById(folder.getUserId()).get().getUsername();

        List<MinioDto> fileList = fileService.getUserFiles(new GetFilesRequest(username, folderPath));
        String breadCrumbs = recoverBreadCrumbs(folderId, folderAccess);
        return new ResponseEntity<>(new PublicAccessDto(new ListOfData(fileList), breadCrumbs), HttpStatus.OK);
    }

    private String recoverBreadCrumbs(Long folderId, int folderAccess){
        List<String> breadList = new ArrayList<>();
        Long currentFolderId = folderId;
        String result = "";
        while (true){
            Long parentFolderId = getParent(currentFolderId);
            Folder folder = null;
            if(parentFolderId == -1) {
                break;
            }
            int parentFolderAccess = getParentFolderAccess(parentFolderId);
            if (parentFolderAccess >= folderAccess) {
                folder = folderRepository.findFolderByFolderId(parentFolderId).get();
                breadList.add(folder.getFolderName());
            }
            currentFolderId = parentFolderId;
        }
        for(String part : breadList){
            result = part + "/" + result;
        }
        int index = result.lastIndexOf("/");
        if(index != -1)
            result = result.substring(0, index);
        return result;
    }
}



















