package com.portal.conecta.checklist.shared.security.solid;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public record PortalUserPrincipal(
    String username,
    Set<String> profiles,
    Long turmaId,
    Set<Long> linkedTurmaIds,
    Long requestedTurmasId,
    String requestedScope)implements UserDetails{
    public static PortalUserPrincipal fromClaims(Claims claims){
        String user = claims.getSubject();
        @SuppressWarnings("unchecked")
                List<String> prof=(List<String>) claims.get("profiles");
        return new PortalUserPrincipal(user,
                Set.copyOf(prof),
                null,
                Set.of(),
                null,
                null);
    }
    public boolean hasProfile(String profile){
        return profile.contains(profile);
    }
    @Override
    public Collection<? extends GrantedAuthority>getAuthorities(){
        return profiles.stream()
                .map(p-> new SimpleGrantedAuthority("ROLE_"+p))
                .toList();
    }
    @Override public String getPassword(){return "";}
    @Override public String getUsername(){return username;}
    @Override public boolean isAccountNonExpired(){return true;}
    @Override public boolean isAccountNonLocked(){return true;}
    @Override public boolean isCredentialsNonExpired(){return true;}
    @Override public boolean isEnabled(){return true;}
}

