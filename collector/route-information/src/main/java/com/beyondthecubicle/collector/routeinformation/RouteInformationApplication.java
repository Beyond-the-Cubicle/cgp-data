package com.beyondthecubicle.collector.routeinformation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RouteInformationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RouteInformationApplication.class, args);
    }

}
