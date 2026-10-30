package com.shalom.controller;

import com.shalom.model.*;
import com.shalom.repository.*;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          Environment environment) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @ModelAttribute("googleEnabled")
    public boolean googleEnabled() {
        String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id", "");
        String clientSecret = environment.getProperty("spring.security.oauth2.client.registration.google.client-secret", "");
        return !clientId.isBlank() && !clientSecret.isBlank();
    }

    @ModelAttribute("user")
    public User emptyUser() {
        return new User();
    }

    @GetMapping("/login")
    public String loginPage(Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/auth/google")
    public String googleRedirect() {
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, BindingResult result,
                           @RequestParam String password_confirmation, Model model) {
        if (user.getName() == null || user.getName().isBlank()) {
            result.rejectValue("name", "error.user", "El nombre es requerido");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            result.rejectValue("email", "error.user", "El email es requerido");
        } else if (userRepository.existsByEmail(user.getEmail())) {
            result.rejectValue("email", "error.user", "Este email ya esta registrado");
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            result.rejectValue("password", "error.user", "La contrasena debe tener al menos 8 caracteres");
        } else if (!user.getPassword().equals(password_confirmation)) {
            result.rejectValue("password", "error.user", "Las contrasenas no coinciden");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        Role userRole = roleRepository.findByName("user").orElseThrow();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(userRole);
        user.setEnabled(true);
        userRepository.save(user);

        return "redirect:/login";
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }
        return "welcome";
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}