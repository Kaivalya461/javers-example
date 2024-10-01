package com.kv.javers_example.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MyChangeDetectionResponseModel {
    private String empId;
    private List<MyChangeFieldsDto> changeFieldsList;
    private Map<String, Object> favoriteDrugsChangeFieldDto;
    private int changeFieldsSize;
}
