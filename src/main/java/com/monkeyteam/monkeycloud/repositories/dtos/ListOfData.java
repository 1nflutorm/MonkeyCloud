package com.monkeyteam.monkeycloud.repositories.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ListOfData {
    List<MinioDto> list;
}
