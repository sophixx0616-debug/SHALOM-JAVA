package com.shalom.security;

import com.shalom.model.Role;
import com.shalom.model.User;
import com.shalom.repository.RoleRepository;
import com.shalom.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de callback de Google OAuth2 (equivalente al flujo de Laravel Socialite).
 *
 * 1. Recibe la informacion real de Google (nombre, correo, Google ID y foto).
 * 2. Busca el usuario por correo; si existe NO se crea otro (se reutiliza la cuenta).
 * 3. Si no existe, crea una cuenta de CLIENTE (rol "user") con una contrasena aleatoria
 *    (el usuario solo entrara por Google) y almacena el Google ID y la foto de perfil.
 * 4. Devuelve un principal que tambien es UserDetails para que el resto de la app funcione igual.
 */
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    public CustomOAuth2UserService(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = delegate.loadUser(userRequest);
        Map<String, Object> attrs = oauthUser.getAttributes();

        String email = (String) attrs.get("email");
        String googleId = (String) attrs.get("sub");
        String picture = (String) attrs.get("picture");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("No se pudo obtener el correo de la cuenta de Google.");
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> createClientUser(attrs));

        boolean touched = false;
        if ((user.getGoogleId() == null || user.getGoogleId().isBlank()) && googleId != null) {
            user.setGoogleId(googleId);
            touched = true;
        }
        if ((user.getPhotoUrl() == null || user.getPhotoUrl().isBlank()) && picture != null) {
            user.setPhotoUrl(picture);
            touched = true;
        }
        if (touched) {
            userRepository.save(user);
        }

        String roleName = user.getRole() != null ? user.getRole().getName() : "user";
        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));

        return new ShalomOAuth2User(oauthUser, user.getEmail(), user.getPassword(),
                user.isEnabled(), authorities);
    }

    private User createClientUser(Map<String, Object> attrs) {
        Role userRole = roleRepository.findByName("user").orElseThrow(
                () -> new IllegalStateException("El rol de cliente no existe en la base de datos."));

        User user = new User();
        user.setName(firstNonBlank(attrs.get("given_name"), attrs.get("name"), "Cliente"));
        user.setLastName(firstNonBlank(attrs.get("family_name"), ""));
        user.setEmail((String) attrs.get("email"));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(userRole);
        user.setEnabled(true);
        user.setGoogleId((String) attrs.get("sub"));
        user.setPhotoUrl((String) attrs.get("picture"));
        return userRepository.save(user);
    }

    private String firstNonBlank(Object first, Object... rest) {
        String value = first != null ? first.toString() : null;
        if (value != null && !value.isBlank()) return value.trim();
        for (Object candidate : rest) {
            String v = candidate != null ? candidate.toString() : null;
            if (v != null && !v.isBlank()) return v.trim();
        }
        return null;
    }
}