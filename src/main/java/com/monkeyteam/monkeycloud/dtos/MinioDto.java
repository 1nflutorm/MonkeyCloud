package com.monkeyteam.monkeycloud.dtos;
import lombok.*;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode
public class MinioDto {
    private String username;
    private Long folderId;
    private Boolean isDir;
    private String path;
    private String name;
    private String breadCrums;
    private String size;
    private Boolean isFavorite;
    private String date;
}
