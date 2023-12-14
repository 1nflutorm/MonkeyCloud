package com.monkeyteam.monkeycloud.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "favorite_files")
@IdClass(FavoriteFile.class)
public class FavoriteFile implements Serializable {
    @Id
    @Column(name = "user_id")
    private long userId;
    @Id
    @Column(name = "folder_id")
    private long folderId;
    @Id
    @Column(name = "file_path")
    private String filePath;
}
