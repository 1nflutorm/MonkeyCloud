package com.monkeyteam.monkeycloud.dtos;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class MinioDto {
    private String username;
    private Boolean isDir;
    private String path;
    private String name;
    private Long size;
    private Boolean isFavorite;
    private String date;
}
