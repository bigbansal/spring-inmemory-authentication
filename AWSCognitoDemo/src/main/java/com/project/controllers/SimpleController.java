package com.project.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {

    @GetMapping(path = "/api/hello")
    public String getResp(){
        return  "Hey authenticated request!";
    }

    @GetMapping(path = "/admin/hello")
    public String geAdmin(){
        return  "Hey Admin request!";
    }

}
