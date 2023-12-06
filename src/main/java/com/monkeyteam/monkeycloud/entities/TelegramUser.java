package com.monkeyteam.monkeycloud.entities;

import lombok.Data;
import org.hibernate.annotations.GeneratorType;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@Table(name = "telegram")
@IdClass(TelegramUser.class)
public class TelegramUser implements Serializable {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    //GET запрос tgId, username, userId
    //обработка пост запроса
}