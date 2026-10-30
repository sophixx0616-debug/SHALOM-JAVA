package com.shalom.controller;

import com.shalom.model.*;
import com.shalom.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          AppointmentRepository appointmentRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        return "users/create";
    }

    @PostMapping
    public String store(@RequestParam(required = false) String name,
                        @RequestParam(required = false) String lastName,
                        @RequestParam(required = false) String phone,
                        @RequestParam(required = false) String email,
                        @RequestParam(required = false) String password,
                        @RequestParam(required = false) String password_confirmation,
                        @RequestParam(value = "role.id", required = false) Long roleId,
                        RedirectAttributes redirectAttributes) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            redirectAttributes.addFlashAttribute("error", "El nombre, el correo y la contraseña son obligatorios.");
            return "redirect:/users/create";
        }
        if (roleId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes seleccionar un rol.");
            return "redirect:/users/create";
        }
        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Ya existe un usuario con ese correo.");
            return "redirect:/users/create";
        }
        if (!password.equals(password_confirmation)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/users/create";
        }
        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "redirect:/users/create";
        }

        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            redirectAttributes.addFlashAttribute("error", "El rol seleccionado no es válido.");
            return "redirect:/users/create";
        }

        User user = new User();
        user.setName(name);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Usuario creado correctamente.");
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("user", userRepository.findById(id).orElseThrow());
        model.addAttribute("roles", roleRepository.findAll());
        return "users/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam(required = false) String name,
                         @RequestParam(required = false) String lastName,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String password,
                         @RequestParam(value = "role.id", required = false) Long roleId,
                         RedirectAttributes redirectAttributes) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/users";
        }
        if (!StringUtils.hasText(name) || !StringUtils.hasText(email)) {
            redirectAttributes.addFlashAttribute("error", "El nombre y el correo son obligatorios.");
            return "redirect:/users/" + id + "/edit";
        }
        if (roleId == null) {
            redirectAttributes.addFlashAttribute("error", "Debes seleccionar un rol.");
            return "redirect:/users/" + id + "/edit";
        }
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            redirectAttributes.addFlashAttribute("error", "Ya existe otro usuario con ese correo.");
            return "redirect:/users/" + id + "/edit";
        }
        if (password != null && !password.isBlank() && password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "redirect:/users/" + id + "/edit";
        }

        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            redirectAttributes.addFlashAttribute("error", "El rol seleccionado no es válido.");
            return "redirect:/users/" + id + "/edit";
        }

        existing.setName(name);
        existing.setLastName(lastName);
        existing.setPhone(phone);
        existing.setEmail(email);
        existing.setRole(role);
        if (password != null && !password.isBlank()) {
            existing.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(existing);

        redirectAttributes.addFlashAttribute("success", "Usuario actualizado correctamente.");
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String destroy(@PathVariable Long id,
                          @RequestParam(required = false) String editar,
                          RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "El usuario no existe.");
            return "redirect:/users";
        }
        if ("1".equals(editar)) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar la sesión que estás usando.");
            return "redirect:/users";
        }
        if (appointmentRepository.existsByUserId(id)) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar: el usuario tiene citas registradas.");
            return "redirect:/users";
        }
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Usuario eliminado correctamente.");
        return "redirect:/users";
    }
}