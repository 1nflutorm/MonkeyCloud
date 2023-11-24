package com.monkeyteam.monkeycloud.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "inheritor_folders")
@IdClass(InheritorFolder.class)
public class InheritorFolder implements Serializable {
    @Id
    @Column(name = "parrent_folder_id")
    private Long ParentFolderId;
    @Id
    @Column(name = "child_folder_id")
    private Long ChildFolderId;
}
