package com.shalom.controller;

import com.shalom.model.Inventory;
import com.shalom.model.InventoryMovement;
import com.shalom.model.User;
import com.shalom.model.enums.MovementType;
import com.shalom.repository.InventoryMovementRepository;
import com.shalom.repository.InventoryRepository;
import com.shalom.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryMovementController {

    private final InventoryMovementRepository movementRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    public InventoryMovementController(InventoryMovementRepository movementRepository,
                                       InventoryRepository inventoryRepository,
                                       UserRepository userRepository) {
        this.movementRepository = movementRepository;
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/movimientos")
    public String allMovements(Model model) {
        List<InventoryMovement> movements = movementRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("movements", movements);
        model.addAttribute("products", inventoryRepository.findAll());
        return "inventory/movements";
    }

    @GetMapping("/{id}/movimientos")
    public String movements(@PathVariable Long id, Model model) {
        Inventory product = inventoryRepository.findById(id).orElseThrow();
        List<InventoryMovement> movements = movementRepository.findByProductIdOrderByCreatedAtDesc(id);
        model.addAttribute("movements", movements);
        model.addAttribute("product", product);
        model.addAttribute("products", inventoryRepository.findAll());
        return "inventory/movements";
    }

    @PostMapping("/{id}/movimientos")
    public String addMovement(@PathVariable Long id,
                              @RequestParam MovementType type,
                              @RequestParam int quantity,
                              @RequestParam(required = false) String reason,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        Inventory product = inventoryRepository.findById(id).orElseThrow();
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setType(type);
        movement.setReason(reason);
        movement.setUser(user);

        int before = product.getStock();

        switch (type) {
            case ENTRADA -> {
                movement.setQuantity(quantity);
                product.setStock(before + quantity);
            }
            case SALIDA -> {
                if (before < quantity) {
                    redirectAttributes.addFlashAttribute("error", "Stock insuficiente. Disponible: " + before);
                    return "redirect:/inventory/" + id + "/movimientos";
                }
                movement.setQuantity(-quantity);
                product.setStock(before - quantity);
            }
            case AJUSTE -> {
                movement.setQuantity(quantity - before);
                product.setStock(quantity);
            }
        }

        movement.setStockBefore(before);
        movement.setStockAfter(product.getStock());
        inventoryRepository.save(product);
        movementRepository.save(movement);

        redirectAttributes.addFlashAttribute("success", "Movimiento registrado correctamente.");
        return "redirect:/inventory/" + id + "/movimientos";
    }
}