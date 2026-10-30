package com.shalom.controller;

import com.shalom.model.Inventory;
import com.shalom.repository.InventoryMovementRepository;
import com.shalom.repository.InventoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public InventoryController(InventoryRepository inventoryRepository,
                               InventoryMovementRepository inventoryMovementRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    @GetMapping
    public String index(Model model) {
        List<Inventory> items = inventoryRepository.findAll();
        model.addAttribute("items", items);
        model.addAttribute("totalProductos", items.size());
        model.addAttribute("stockBajo", inventoryRepository.findLowStock().size());
        model.addAttribute("sinStock", items.stream().filter(i -> i.getStock() == 0).count());
        return "inventory/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("item", new Inventory());
        return "inventory/create";
    }

    @PostMapping
    public String store(@ModelAttribute Inventory item, RedirectAttributes redirectAttributes) {
        if (!StringUtils.hasText(item.getProductName())) {
            redirectAttributes.addFlashAttribute("error", "El nombre del producto es obligatorio.");
            return "redirect:/inventory/create";
        }
        if (item.getPrice() == null || item.getPrice().signum() < 0) {
            redirectAttributes.addFlashAttribute("error", "El precio debe ser un valor válido.");
            return "redirect:/inventory/create";
        }
        if (item.getMinStock() < 0) {
            redirectAttributes.addFlashAttribute("error", "El stock mínimo no puede ser negativo.");
            return "redirect:/inventory/create";
        }
        item.setStock(0);
        inventoryRepository.save(item);
        redirectAttributes.addFlashAttribute("success", "Producto agregado correctamente");
        return "redirect:/inventory";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("item", inventoryRepository.findById(id).orElseThrow());
        return "inventory/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Inventory item, RedirectAttributes redirectAttributes) {
        Inventory existing = inventoryRepository.findById(id).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", "El producto no existe.");
            return "redirect:/inventory";
        }
        if (!StringUtils.hasText(item.getProductName())) {
            redirectAttributes.addFlashAttribute("error", "El nombre del producto es obligatorio.");
            return "redirect:/inventory/" + id + "/edit";
        }
        if (item.getPrice() == null || item.getPrice().signum() < 0) {
            redirectAttributes.addFlashAttribute("error", "El precio debe ser un valor válido.");
            return "redirect:/inventory/" + id + "/edit";
        }
        existing.setProductName(item.getProductName());
        existing.setBrand(item.getBrand());
        existing.setCategory(item.getCategory());
        existing.setDescription(item.getDescription());
        existing.setMinStock(item.getMinStock());
        existing.setPrice(item.getPrice());
        if (StringUtils.hasText(item.getImage())) {
            existing.setImage(item.getImage());
        }
        inventoryRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente");
        return "redirect:/inventory";
    }

    @PostMapping("/{id}/delete")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (inventoryMovementRepository.existsByProductId(id)) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar: el producto tiene movimientos de inventario registrados.");
            return "redirect:/inventory";
        }
        inventoryRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente");
        return "redirect:/inventory";
    }
}