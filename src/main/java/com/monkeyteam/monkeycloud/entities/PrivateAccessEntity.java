package com.monkeyteam.monkeycloud.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "private_access")
@IdClass(PrivateAccessEntity.class)
public class PrivateAccessEntity implements Serializable {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Id
    @Column(name = "folder_id")
    private Long folderId;
}
