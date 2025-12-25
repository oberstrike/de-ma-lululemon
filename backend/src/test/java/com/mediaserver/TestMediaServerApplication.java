package com.mediaserver;

import com.mediaserver.config.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

/**
 * Test application that uses Testcontainers for PostgreSQL. Run this to start the application with
 * a containerized database for local development.
 */
public class TestMediaServerApplication {

    public static void main(String[] args) {
        SpringApplication.from(MediaServerApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
