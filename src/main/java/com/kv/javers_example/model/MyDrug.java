package com.kv.javers_example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.javers.core.metamodel.annotation.Id;

@Data
@AllArgsConstructor
public class MyDrug {
    @Id
    private String ndcCode;
    private String name;
    private double amount;
}
