package com.monkeyteam.monkeycloud.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "inheritor_folder")
public class InheritorFolder {
    @Id
    @Column(name = "parent_folder_id")
    private Long ParentFolderId;
    @Id
    @Column(name = "child_folder_id")
    private Long ChildFolderId;
}
