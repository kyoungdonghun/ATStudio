package com.atstudio.atstudio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AtStudioApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtStudioApplication.class, args);
    }

}