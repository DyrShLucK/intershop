package com.intershop.intershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/intershop")
public class OrderController {

    public String orderCreate(Model model){
        return "order";
    }
}
