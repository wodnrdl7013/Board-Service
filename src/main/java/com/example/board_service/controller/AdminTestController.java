package com.example.board_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminTestController {

    @GetMapping("/ping")
    public String ping() {
        return "ok";
    }
}
