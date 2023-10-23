package com.monkeyteam.monkeycloud.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long user_id;
    @Column(name = "username")
    private String username;
    @Column(name = "user_password")
    private String password;
    @Column(name = "user_second_name ")
    private String second_name;
    @Column(name = "user_first_name")
    private String name;
    @Column(name = "user_father_name ")
    private String last_name;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> roles;
}
