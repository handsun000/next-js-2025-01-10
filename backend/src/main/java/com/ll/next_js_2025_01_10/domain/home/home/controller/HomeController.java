package com.ll.next_js_2025_01_10.domain.home.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Tag(name = "HomeController", description = "홈 컨트롤러")
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    @Operation(summary = "메인 페이지")
    public String home() {
        return "API 서버 입니다.";
    }
}

