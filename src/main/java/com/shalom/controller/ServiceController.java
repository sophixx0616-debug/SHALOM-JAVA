package com.shalom.controller;

import com.shalom.model.Service;
import com.shalom.model.User;
import com.shalom.repository.ServiceRepository;
import com.shalom.repository.UserRepository;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/services")
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public ServiceController(ServiceRepository serviceRepository,
                             UserRepository userRepository) {
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    /**
     * Página pública de servicios.
     */
    @GetMapping
    public String index(Model model) {

        List<Service> services = serviceRepository.findByStatusTrueOrderByNameAsc();

        model.addAttribute("services", services);

        return "services/index";
    }

    /**
     * Página de administración de servicios.
     */
    @GetMapping("/admin")
    public String adminServices(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        if (user.getRole() == null ||
                !"admin".equalsIgnoreCase(user.getRole().getName())) {
            return "redirect:/";
        }

        model.addAttribute(
                "services",
                serviceRepository.findAll()
        );

        return "services/admin";
    }

    /**
     * Formulario para crear servicio.
     */
    @GetMapping("/create")
    public String create(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        if (user.getRole() == null ||
                !"admin".equalsIgnoreCase(user.getRole().getName())) {
            return "redirect:/";
        }

        model.addAttribute("service", new Service());

        return "services/create";
    }

    /**
     * Guardar nuevo servicio.
     */
    @PostMapping
    public String store(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute Service service,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        if (user.getRole() == null ||
                !"admin".equalsIgnoreCase(user.getRole().getName())) {
            return "redirect:/";
        }

        serviceRepository.save(service);

        redirectAttributes.addFlashAttribute(
                "success",
                "Servicio creado correctamente."
        );

        return "redirect:/services/admin";
    }

    /**
     * Formulario para editar servicio.
     */
    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        if (user.getRole() == null ||
                !"admin".equalsIgnoreCase(user.getRole().getName())) {
            return "redirect:/";
        }

        Service service = serviceRepository.findById(id)
                .orElseThrow();

        model.addAttribute("service", service);

        return "services/edit";
    }

    /**
     * Actualizar servicio.
     */
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute Service service,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        if (user.getRole() == null ||
                !"admin".equalsIgnoreCase(user.getRole().getName())) {
            return "redirect:/";
        }

        Service existing = serviceRepository.findById(id)
                .orElseThrow();

        existing.setName(service.getName());
        existing.setDescription(service.getDescription());
        existing.setPrice(service.getPrice());
        existing.setDuration(service.getDuration());
        existing.setCategory(service.getCategory());
        existing.setStatus(service.isStatus());
        existing.setImage(service.getImage());

        serviceRepository.save(existing);

        redirectAttributes.addFlashAttribute(
                "success",
                "Servicio actualizado correctamente."
        );

        return "redirect:/services/admin";
    }

    /**
     * Eliminar servicio.
     */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow();

        if (user.getRole() == null ||
                !"admin".equalsIgnoreCase(user.getRole().getName())) {
            return "redirect:/";
        }

        serviceRepository.deleteById(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Servicio eliminado correctamente."
        );

        return "redirect:/services/admin";
    }
}