package com.shalom.controller;

import com.shalom.model.PasswordResetToken;
import com.shalom.model.User;
import com.shalom.repository.PasswordResetTokenRepository;
import com.shalom.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Recuperacion de contrasena (enlace real y funcional, token de un solo uso).
 *
 * En este entorno local no hay servidor SMTP configurado, por lo que el enlace
 * de restablecimiento se muestra en pantalla tras solicitar el correo. Cuando se
 * conecte un SMTP (spring.mail.*) basta con enviar el enlace por correo; el flujo
 * del token ya está completo y seguro (expira en 1 hora y solo sirve una vez).
 */
@Controller
public class PasswordResetController {

    private static final long EXPIRATION_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetController(UserRepository userRepository,
                                   PasswordResetTokenRepository tokenRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/forgot-password")
    public String forgotForm() {
        return "auth/forgot";
    }

    @PostMapping("/forgot-password")
    public String forgot(@RequestParam String email, Model model) {
        model.addAttribute("frEmail", email);
        boolean exists = email != null && userRepository.existsByEmail(email.trim());

        if (exists) {
            tokenRepository.deleteByEmail(email.trim());
            PasswordResetToken token = new PasswordResetToken(
                    email.trim(), generateToken(), LocalDateTime.now().plusHours(EXPIRATION_HOURS));
            tokenRepository.save(token);
            model.addAttribute("resetLink", "/password/reset/" + token.getToken());
            model.addAttribute("resetSent", true);
        } else {
            model.addAttribute("emailNotFound", true);
        }
        return "auth/forgot";
    }

    @GetMapping("/password/reset/{token}")
    public String resetForm(@PathVariable String token, Model model) {
        if (!isValidToken(token)) {
            model.addAttribute("invalidToken", true);
            return "auth/reset";
        }
        model.addAttribute("token", token);
        return "auth/reset";
    }

    @PostMapping("/password/reset/{token}")
    public String reset(@PathVariable String token,
                        @RequestParam String password,
                        @RequestParam String password_confirmation,
                        RedirectAttributes redirectAttributes) {
        if (!isValidToken(token)) {
            redirectAttributes.addFlashAttribute("error", "El enlace no es valido o ya expiro.");
            return "redirect:/forgot-password";
        }
        if (password == null || password.length() < 8) {
            return "redirect:/password/reset/" + token + "?error=password";
        }
        if (!password.equals(password_confirmation)) {
            return "redirect:/password/reset/" + token + "?error=match";
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token).orElseThrow();
        User user = userRepository.findByEmail(resetToken.getEmail()).orElse(null);

        if (user == null) {
            tokenRepository.delete(resetToken);
            redirectAttributes.addFlashAttribute("error", "No se encontro la cuenta asociada.");
            return "redirect:/forgot-password";
        }

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        tokenRepository.delete(resetToken);

        redirectAttributes.addFlashAttribute("success", "Contrasena actualizada correctamente. Ya puedes iniciar sesion.");
        return "redirect:/login";
    }

    private boolean isValidToken(String token) {
        if (token == null || token.isBlank()) return false;
        return tokenRepository.findByToken(token)
                .map(t -> t.getExpiresAt() != null && t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}