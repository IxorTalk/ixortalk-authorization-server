package com.ixortalk.authorization.server.rest;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.ixortalk.authorization.server.domain.UserProfile;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

class PrincipalDTO {

    private UserProfile userProfile;
    private Collection<GrantedAuthority> authorities;
    private Object userInfo;

    PrincipalDTO(UserProfile userProfile, Collection<GrantedAuthority> authorities, Object userInfo) {
        this.userProfile = userProfile;
        this.authorities = authorities;
        this.userInfo = userInfo;
    }

    @JsonUnwrapped
    public UserProfile getUserProfile() {
        return userProfile;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Object getUserInfo() {
        return userInfo;
    }
}
