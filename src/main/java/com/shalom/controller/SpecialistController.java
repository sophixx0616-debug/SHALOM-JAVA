package com.shalom.controller;

import com.shalom.model.Specialist;
import com.shalom.repository.SpecialistRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/specialists")
public class SpecialistController {

    private final SpecialistRepository specialistRepository;

    public SpecialistController(SpecialistRepository specialistRepository) {
        this.specialistRepository = specialistRepository;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("specialists", specialistRepository.findAll());
        if (userDetails != null) {
            model.addAttribute("isAdmin",
                    userDetails.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
        }
        return "specialists/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("specialist", new Specialist());
        return "specialists/create";
    }

    @PostMapping
    public String store(@ModelAttribute Specialist specialist, RedirectAttributes redirectAttributes) {
        specialistRepository.save(specialist);
        redirectAttributes.addFlashAttribute("success", "Especialista creada correctamente");
        return "redirect:/specialists";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("specialist", specialistRepository.findById(id).orElseThrow());
        return "specialists/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute Specialist specialist, RedirectAttributes redirectAttributes) {
        Specialist existing = specialistRepository.findById(id).orElseThrow();
        existing.setName(specialist.getName());
        existing.setSpecialty(specialist.getSpecialty());
        existing.setDescription(specialist.getDescription());
        specialistRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Especialista actualizada correctamente");
        return "redirect:/specialists";
    }

    @PostMapping("/{id}/delete")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        specialistRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Especialista eliminada correctamente");
        return "redirect:/specialists";
    }
}
