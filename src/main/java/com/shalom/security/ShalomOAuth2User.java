package com.shalom.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * Principal de autenticacion usada tras un login con Google.
 * Implementa OAuth2User y UserDetails a la vez para que los controladores
 * del proyecto (que usan @AuthenticationPrincipal UserDetails) sigan
 * funcionando igual que con un login por formulario.
 * getName()/getUsername() devuelven el correo del usuario.
 */
public class ShalomOAuth2User implements OAuth2User, UserDetails {

    private final OAuth2User delegate;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public ShalomOAuth2User(OAuth2User delegate, String email, String password,
                            boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.delegate = delegate;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}