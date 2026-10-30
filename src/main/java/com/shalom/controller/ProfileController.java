package com.shalom.controller;

import com.shalom.model.*;
import com.shalom.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoyaltyCardRepository loyaltyCardRepository;
    private final AppointmentRepository appointmentRepository;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                             LoyaltyCardRepository loyaltyCardRepository,
                             AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loyaltyCardRepository = loyaltyCardRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/profile")
    public String edit(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("isAdmin", user.getRole() != null && "admin".equalsIgnoreCase(user.getRole().getName()));

        if (user.getRole() == null || !"admin".equalsIgnoreCase(user.getRole().getName())) {
            LoyaltyCard card = loyaltyCardRepository.findByUserId(user.getId()).orElseGet(() -> {
                LoyaltyCard nc = new LoyaltyCard();
                nc.setUser(user);
                nc.setStamps(0);
                nc.setAvailableRewards(0);
                return loyaltyCardRepository.save(nc);
            });
            model.addAttribute("card", card);
            model.addAttribute("progressPct", (int) Math.round(card.getStamps() * 100.0 / 9));
            model.addAttribute("remainingStamps", Math.max(0, 9 - card.getStamps()));
        }
        return "profile/edit";
    }

    @PostMapping("/profile")
    public String update(@AuthenticationPrincipal UserDetails userDetails,
                         @RequestParam String name,
                         @RequestParam String lastName,
                         @RequestParam String phone,
                         @RequestParam String email,
                         RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        if (userRepository.existsByEmailAndIdNot(email, user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Ya existe otro usuario con ese correo.");
            return "redirect:/profile";
        }
        user.setName(name);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setEmail(email);
        userRepository.save(user);
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken newAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        email, auth.getCredentials(), auth.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(newAuth);
        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String updatePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String current_password,
                                 @RequestParam String password,
                                 @RequestParam String password_confirmation,
                                 RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        if (!passwordEncoder.matches(current_password, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta.");
            return "redirect:/profile";
        }

        if (!password.equals(password_confirmation)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Contraseña actualizada correctamente.");
        return "redirect:/profile";
    }
}
