package com.mediaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MediaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaServerApplication.class, args);
    }
}
