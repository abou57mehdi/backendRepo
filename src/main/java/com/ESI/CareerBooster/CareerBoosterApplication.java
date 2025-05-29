package com.ESI.CareerBooster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class CareerBoosterApplication {
    public static void main(String[] args) {
        SpringApplication.run(CareerBoosterApplication.class, args);
    }
}
