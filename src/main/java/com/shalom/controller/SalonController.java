package com.shalom.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SalonController {

    @GetMapping("/citas")
    public String citas() {
        return "redirect:/appointments"; 
    }

    @GetMapping("/clientes")
    public String clientes() {
        return "redirect:/users"; 
    }

    @GetMapping("/servicios")
    public String servicios() {
        return "redirect:/services"; 
    }

    @GetMapping("/inventario")
    public String inventario() {
        return "redirect:/inventory"; 
    }

    @GetMapping("/ventas")
    public String ventas() {
        return "redirect:/carrito";
    }

    @GetMapping("/configuracion")
    public String configuracion() {
        return "redirect:/profile"; 
    }
}