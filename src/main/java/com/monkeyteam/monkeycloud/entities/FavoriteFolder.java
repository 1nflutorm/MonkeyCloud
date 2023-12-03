package com.monkeyteam.monkeycloud.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "favorite_folders")
@IdClass(FavoriteFolder.class)
public class FavoriteFolder implements Serializable {
    @Id
    @Column(name = "user_id")
    private long userId;
    @Id
    @Column(name = "folder_id")
    private long folderId;
}