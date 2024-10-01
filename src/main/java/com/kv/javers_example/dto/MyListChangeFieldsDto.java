package com.kv.javers_example.dto;

import lombok.Data;

import java.util.List;

@Data
public class MyListChangeFieldsDto {
    private String listElementChangeType; //Added, Removed, Updated
    List<MyChangeFieldsDto> listChangeFieldList;
}
