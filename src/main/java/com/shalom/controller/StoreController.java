package com.shalom.controller;

import com.shalom.repository.InventoryRepository;
import com.shalom.repository.ServiceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StoreController {

    private final InventoryRepository inventoryRepository;
    private final ServiceRepository serviceRepository;

    public StoreController(InventoryRepository inventoryRepository, ServiceRepository serviceRepository) {
        this.inventoryRepository = inventoryRepository;
        this.serviceRepository = serviceRepository;
    }

    @GetMapping("/tienda")
    public String tienda(Model model) {
        model.addAttribute("productos", inventoryRepository.findAll());
        model.addAttribute("servicios", serviceRepository.findAll());
        return "store/index";
    }
}