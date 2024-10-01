package com.kv.javers_example.controller;

import com.kv.javers_example.dto.MyChangeDetectionResponseModel;
import com.kv.javers_example.service.JaversTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JaversTest {
    @Autowired private JaversTestService javersTestService;

    @GetMapping("/javers-test")
    public ResponseEntity<List<MyChangeDetectionResponseModel>> javersTest() {
        return ResponseEntity.ok(javersTestService.nestedListTest());
    }
}
