package com.monkeyteam.monkeycloud.repositories.dtos;
import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class MinioDto {
    private String username;
    private Boolean isDir;
    private String path;
    private String name;
}
