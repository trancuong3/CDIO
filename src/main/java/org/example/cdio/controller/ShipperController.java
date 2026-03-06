package org.example.cdio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShipperController {

    @GetMapping("/shipper/dashboard")
    public String dashboard() {
        return "shipper/dashboard";
    }
}