package com.monkeyteam.monkeycloud.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PublicAccessDto {
    ListOfData fileList;
    String breadCrumbs;
}
