package com.citynote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan("com.citynote.mapper")
public class CityNoteApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CityNoteApplication.class, args);
    }
} 