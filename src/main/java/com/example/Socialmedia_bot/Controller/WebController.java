package com.example.Socialmedia_bot.Controller;

// src/main/java/com/yourpackage/controller/WebController.java

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "index.html"; // serves static/index.html
    }
}