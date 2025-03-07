package org.example.filestorageapi.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.filestorageapi.entity.User;
import org.example.filestorageapi.utils.Roles;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails, CredentialsContainer, Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    private final Long id;

    private final String username;

    @JsonIgnore
    private String password;

    private final Set<Roles> roles;

    public CustomUserDetails(User user) {
        this.id = (long) user.getId();
        this.username = user.getUsername();
        this.password = user.getEncryptedPassword();
        this.roles = Collections.singleton(user.getRoles());
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
