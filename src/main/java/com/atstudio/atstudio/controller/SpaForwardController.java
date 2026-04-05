package com.atstudio.atstudio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping("/{path:^(?!api|uploads|swagger-ui|v3|oauth2|assets|.*\\..*).*$}/**")
    public String forward() {
        return "forward:/index.html";
    }
}
