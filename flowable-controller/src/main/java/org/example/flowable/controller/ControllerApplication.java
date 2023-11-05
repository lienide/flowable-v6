package org.example.flowable.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControllerApplication.class, args);
    }

    @RestController
    static class TestController {

        @RequestMapping("/s1")
        public Object s1(@RequestBody Map<String, Object> params) throws InterruptedException {
            Objects.requireNonNull(params.get("name"), "name missing");

            System.out.println(">>> s1");
            TimeUnit.SECONDS.sleep(1);

            HashMap<String, Object> response = new HashMap<>();
            response.put("phone", "138");
            response.put("nv", null);
            response.put("int", 10);
            response.put("double", 10.0);
            return response;
        }
        @RequestMapping("/s2")
        public Object s2(@RequestBody Map<String, Object> params) throws InterruptedException {
            Objects.requireNonNull(params.get("phone"), "phone missing");

            System.out.println(">>> s2");
            TimeUnit.SECONDS.sleep(2);

            HashMap<String, Object> response = new HashMap<>();
            response.put("k2", "v2");
            return response;
        }
        @RequestMapping("/s3")
        public Object s3(@RequestBody Map<String, Object> params) throws InterruptedException {
            Objects.requireNonNull(params.get("phone"), "phone missing");

            System.out.println(">>> s3");
            TimeUnit.SECONDS.sleep(3);

            HashMap<String, Object> response = new HashMap<>();
            response.put("k3", "v3");
            return response;
        }
        @RequestMapping("/s4")
        public Object s4(@RequestBody Map<String, Object> params) throws InterruptedException {
            Objects.requireNonNull(params.get("phone"), "phone missing");

            System.out.println(">>> s4");
            TimeUnit.SECONDS.sleep(4);

            HashMap<String, Object> response = new HashMap<>();
            response.put("k4", "v4");
            return response;
        }
    }
}
