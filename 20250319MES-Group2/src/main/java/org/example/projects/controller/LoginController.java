package org.example.projects.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
public class LoginController {
    // 로그인 페이지 요청
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }
}