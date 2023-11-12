package com.monkeyteam.monkeycloud.entities;

import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Data
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Column(name = "user_id")
    private long user_id;
    @Id
    @Column(name = "user_token")
    private String token;
    @Column(name = "expire_date")
    private Instant expiryDate;

}
