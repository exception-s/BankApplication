package com.BankApp.localbankapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Alexander Brazhkin
 */
@RestController
public class TestController {

    @GetMapping("/public/hello")
    public String publicHello() {
        return "Public Hello World!";
    }

    @GetMapping("/private/hello")
    public String privateHello() {
        return "Private Hello World!";
    }
}