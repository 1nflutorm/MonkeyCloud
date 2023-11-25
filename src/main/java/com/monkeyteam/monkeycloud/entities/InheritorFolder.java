package com.monkeyteam.monkeycloud.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "inheritor_folder")
@IdClass(InheritorFolder.class)
public class InheritorFolder implements Serializable {
    @Id
    @Column(name = "parent_folder_id")
    private Long ParentFolderId;
    @Id
    @Column(name = "child_folder_id")
    private Long ChildFolderId;
}