package com.example.ari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AriApplication {

    public static void main(String[] args) {
        SpringApplication.run(AriApplication.class, args);
    }

}
