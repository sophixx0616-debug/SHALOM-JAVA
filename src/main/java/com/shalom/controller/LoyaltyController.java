package com.shalom.controller;

import com.shalom.model.*;
import com.shalom.repository.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoyaltyController {

    private final LoyaltyCardRepository loyaltyCardRepository;
    private final UserRepository userRepository;

    public LoyaltyController(LoyaltyCardRepository loyaltyCardRepository, UserRepository userRepository) {
        this.loyaltyCardRepository = loyaltyCardRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/mi-tarjeta")
    public String show(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        LoyaltyCard card = loyaltyCardRepository.findByUserId(user.getId()).orElseGet(() -> {
            LoyaltyCard nc = new LoyaltyCard();
            nc.setUser(user);
            nc.setStamps(0);
            nc.setAvailableRewards(0);
            return loyaltyCardRepository.save(nc);
        });
        model.addAttribute("card", card);
        model.addAttribute("user", user);
        model.addAttribute("remainingStamps", Math.max(0, 9 - card.getStamps()));
        model.addAttribute("progressPct", (int) Math.round(card.getStamps() * 100.0 / 9));
        return "loyalty/show";
    }

    @GetMapping("/fidelizacion")
    public String index(Model model) {
        model.addAttribute("cards", loyaltyCardRepository.findAll());
        return "loyalty/index";
    }

    @PostMapping("/fidelizacion/{userId}/sello")
    public String addStamp(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(userId).orElseThrow();
        LoyaltyCard card = loyaltyCardRepository.findByUserId(userId).orElseGet(() -> {
            LoyaltyCard nc = new LoyaltyCard();
            nc.setUser(user);
            nc.setStamps(0);
            nc.setAvailableRewards(0);
            return loyaltyCardRepository.save(nc);
        });

        int rewardsBefore = card.getAvailableRewards();
        card.addStamp();
        loyaltyCardRepository.save(card);

        if (card.getAvailableRewards() > rewardsBefore) {
            redirectAttributes.addFlashAttribute("success", "Tarjeta completada. La clienta tiene un servicio gratuito disponible.");
        } else {
            redirectAttributes.addFlashAttribute("success", "Sello agregado correctamente a la tarjeta de fidelidad.");
        }
        return "redirect:/fidelizacion";
    }
}
