package com.monkeyteam.monkeycloud.entities;

import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Data
@Table(name = "folders")
public class Folder {
    /*folder_id bigserial primary key,
    user_id int,
    folder_path varchar(255) not null,
    folder_name varchar(255),
    folder_access int not null default 1,*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private long folderId;
    @Column(name = "user_id")
    private long userId;
    @Column(name = "folder_path")
    private String folderPath;
    @Column(name = "folder_name")
    private String folderName;
    @Column(name = "folder_access")
    private int folderAccess;
}