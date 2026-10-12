package com.tecnologico.aires.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class TestController {

    @GetMapping("/test-each")
    public String testEach(Model model) {
        model.addAttribute("items", List.of("Uno", "Dos", "Tres"));
        return "test-each";
    }
}