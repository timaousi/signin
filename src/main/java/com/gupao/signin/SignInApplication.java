package com.gupao.signin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.UnknownHostException;

@SpringBootApplication
public class SignInApplication {

    public static void main(String[] args) throws UnknownHostException {

        SpringApplication.run(SignInApplication.class, args);
    }

}
