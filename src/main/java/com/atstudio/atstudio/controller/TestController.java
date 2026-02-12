package com.atstudio.atstudio.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public String test(){
        return "To infinity and beyond!";
    }

    @GetMapping("/health")
    // 추가: 서버 상태 체크용 (배포 환경에서 유용)
    public String health() {
        return "OK";
    }
}
