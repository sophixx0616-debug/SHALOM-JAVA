package com.shalom.controller;

import com.shalom.model.*;
import com.shalom.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/carrito")
public class CartController {

    private final InventoryRepository inventoryRepository;
    private final ServiceRepository serviceRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final UserRepository userRepository;

    public CartController(InventoryRepository inventoryRepository,
                          ServiceRepository serviceRepository,
                          InventoryMovementRepository inventoryMovementRepository,
                          UserRepository userRepository) {
        this.inventoryRepository = inventoryRepository;
        this.serviceRepository = serviceRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.userRepository = userRepository;
    }

    private BigDecimal total(Map<String, Map<String, Object>> cart) {
        return cart.values().stream()
                .map(item -> ((BigDecimal) item.get("price")).multiply(BigDecimal.valueOf((int) item.get("quantity"))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @GetMapping
    public String index(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> cart = (Map<String, Map<String, Object>>) session.getAttribute("cart");
        if (cart == null) cart = new LinkedHashMap<>();
        model.addAttribute("cart", cart);
        model.addAttribute("total", total(cart));
        return "cart/index";
    }

    @PostMapping("/anadir/{id}")
    public String add(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Inventory product = inventoryRepository.findById(id).orElseThrow();

        if (product.getStock() <= 0) {
            redirectAttributes.addFlashAttribute("error", "¡Lo sentimos! No hay stock de " + product.getProductName());
            return "redirect:/carrito";
        }

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> cart = (Map<String, Map<String, Object>>) session.getAttribute("cart");
        if (cart == null) cart = new LinkedHashMap<>();

        String key = String.valueOf(id);
        if (cart.containsKey(key)) {
            int currentQty = (int) cart.get(key).get("quantity");
            if (currentQty + 1 > product.getStock()) {
                redirectAttributes.addFlashAttribute("error", "No puedes añadir más; solo quedan " + product.getStock() + " unidades.");
                return "redirect:/carrito";
            }
            cart.get(key).put("quantity", currentQty + 1);
        } else {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", product.getProductName());
            item.put("quantity", 1);
            item.put("price", product.getPrice());
            item.put("image", product.getImage() != null ? product.getImage() : "default-product.png");
            item.put("type", "product");
            cart.put(key, item);
        }

        session.setAttribute("cart", cart);
        redirectAttributes.addFlashAttribute("success", product.getProductName() + " se añadió al carrito.");
        return "redirect:/carrito";
    }

    @PostMapping("/eliminar/{id}")
    public String remove(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> cart = (Map<String, Map<String, Object>>) session.getAttribute("cart");
        if (cart != null) {
            cart.remove(id);
            session.setAttribute("cart", cart);
        }
        redirectAttributes.addFlashAttribute("success", "Producto eliminado.");
        return "redirect:/carrito";
    }

    @PostMapping("/anadir-servicio/{id}")
    public String addService(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Service service = serviceRepository.findById(id).orElseThrow();

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> cart = (Map<String, Map<String, Object>>) session.getAttribute("cart");
        if (cart == null) cart = new LinkedHashMap<>();

        String key = "service_" + id;
        if (cart.containsKey(key)) {
            redirectAttributes.addFlashAttribute("error", "Este servicio ya está en tu agenda de la bolsa.");
            return "redirect:/carrito";
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", service.getName() + " (Servicio)");
        item.put("quantity", 1);
        item.put("price", service.getPrice());
        item.put("image", "service-icon");
        item.put("type", "service");
        cart.put(key, item);

        session.setAttribute("cart", cart);
        redirectAttributes.addFlashAttribute("success", "¡" + service.getName() + " se añadió a tu bolsa de experiencias!");
        return "redirect:/carrito";
    }

    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal UserDetails userDetails,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> cart = (Map<String, Map<String, Object>>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El carrito está vacío.");
            return "redirect:/carrito";
        }

        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para completar la compra.");
            return "redirect:/carrito";
        }

        for (var entry : cart.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> item = entry.getValue();
            if ("product".equals(item.get("type"))) {
                Long productId = Long.parseLong(key);
                int qty = (int) item.get("quantity");
                Inventory product = inventoryRepository.findById(productId).orElse(null);
                if (product == null || product.getStock() < qty) {
                    redirectAttributes.addFlashAttribute("error", "No hay suficiente stock de " + item.get("name") + ".");
                    return "redirect:/carrito";
                }
            }
        }

        for (var entry : cart.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> item = entry.getValue();
            if ("product".equals(item.get("type"))) {
                Long productId = Long.parseLong(key);
                int qty = (int) item.get("quantity");
                Inventory product = inventoryRepository.findById(productId).orElse(null);
                if (product != null) {
                    int before = product.getStock();
                    product.setStock(before - qty);
                    inventoryRepository.save(product);
                    InventoryMovement movement = new InventoryMovement();
                    movement.setProduct(product);
                    movement.setType(com.shalom.model.enums.MovementType.SALIDA);
                    movement.setQuantity(-qty);
                    movement.setStockBefore(before);
                    movement.setStockAfter(product.getStock());
                    movement.setReason("Venta por carrito");
                    movement.setUser(user);
                    inventoryMovementRepository.save(movement);
                }
            }
        }

        session.removeAttribute("cart");
        redirectAttributes.addFlashAttribute("success", "Compra realizada exitosamente. ¡Gracias!");
        return "redirect:/carrito";
    }
}
