package com.BankApp.localbankapp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Alexander Brazhkin
 */
@Controller
@Tag(name = "Корневой роутинг", description = "Перенаправление на Swagger UI")
public class RootController {

    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui/index.html";
    }
}