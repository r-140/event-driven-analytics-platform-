package com.example.customer.adapter.in.web;


import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/customers")
public class CustomerController {


    @GetMapping("/health")
    public String health() {

        return "customer-service is running";

    }

}