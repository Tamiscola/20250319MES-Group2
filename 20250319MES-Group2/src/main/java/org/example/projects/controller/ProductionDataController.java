package org.example.projects.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Log4j2
@RequestMapping("/data")
public class ProductionDataController {
    @GetMapping("/list")
    public String list(Model model) {
        return "production-data";
    }
}
