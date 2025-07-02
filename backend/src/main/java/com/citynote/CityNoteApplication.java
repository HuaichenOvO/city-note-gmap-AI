package com.citynote;

import com.citynote.service.UserService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan("com.citynote.mapper")
public class CityNoteApplication implements CommandLineRunner {
    
    @Autowired
    private UserService userService;
    
    public static void main(String[] args) {
        SpringApplication.run(CityNoteApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 确保所有用户都有UserProfile
        userService.ensureAllUsersHaveProfile();
    }
} 