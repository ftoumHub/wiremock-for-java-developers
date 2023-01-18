package com.learnwiremock.pairec.utils;

import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class ApiUserDetails implements UserDetails {

    private static final long serialVersionUID = -7723292944727025106L;
    private Long id;
    private final String username;
    private final String password;
    private final Collection<SimpleGrantedAuthority> authorities;
    private String numeroSocietaireMaif;
    private String idPersonneMaif;
    private String idEmployeMaif;

    public ApiUserDetails(String username, String password, Collection<SimpleGrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean hasRole(GrantedAuthority auth) {
        return this.authorities.contains(auth);
    }

    public String toString() {
        return this.getClass().getSimpleName() + ": " + this.getUsername();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public String getRolesAsString() {
        return (String)this.authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
    }

    public Collection<String> getRolesWithoutRoleTag() {
        return (Collection)this.authorities.stream().map(Object::toString).map((x) -> {
            return x.startsWith("ROLE_") ? x.replaceFirst("ROLE_", "") : x;
        }).collect(Collectors.toList());
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    public String getNumeroSocietaireMaif() {
        return this.numeroSocietaireMaif;
    }

    public void setNumeroSocietaireMaif(String numeroSocietaireMaif) {
        this.numeroSocietaireMaif = numeroSocietaireMaif;
    }

    public String getIdPersonneMaif() {
        return this.idPersonneMaif;
    }

    public void setIdPersonneMaif(String idPersonneMaif) {
        this.idPersonneMaif = idPersonneMaif;
    }

    public String getIdEmployeMaif() {
        return this.idEmployeMaif;
    }

    public void setIdEmployeMaif(String idEmployeMaif) {
        this.idEmployeMaif = idEmployeMaif;
    }
}

