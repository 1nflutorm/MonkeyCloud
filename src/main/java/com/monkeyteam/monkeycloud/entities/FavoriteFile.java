package com.monkeyteam.monkeycloud.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "favorite_files")
public class FavoriteFile {
    @Id
    @Column(name = "user_id")
    private long userId;
    @Id
    @Column(name = "folder_id")
    private long folderId;
    @Column(name = "file_path")
    private String filePath;
}
