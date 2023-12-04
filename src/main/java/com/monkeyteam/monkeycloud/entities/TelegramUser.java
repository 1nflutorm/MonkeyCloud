package com.monkeyteam.monkeycloud.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "telegram")
public class TelegramUser {
    @Column(name = "user_id")
    private Long userId;
    @Id
    @Column(name = "chat_id")
    private Long chatId;

    //GET запрос tgId, username, userId
    //обработка пост запроса
}