package com.monkeyteam.monkeycloud.repositories.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionControl {
    String username;
    boolean active;
}
