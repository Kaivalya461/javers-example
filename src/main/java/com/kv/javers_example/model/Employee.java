package com.kv.javers_example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.javers.core.metamodel.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    @Id
    private int id;

    private String name;

    private int salary;

    private int age;

    private Employee boss;

    private List<Employee> subordinates = new ArrayList<>();

    private List<MyDrug> favoriteDrugs = new ArrayList<>();

    private Address primaryAddress;

    private Set<String> skills;

    public Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }
}