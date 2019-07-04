package com.ixortalk.authorization.server.domain;

import com.ixortalk.test.builder.ReflectionInstanceTestBuilder;

import static org.springframework.test.util.ReflectionTestUtils.setField;

public class AuthorityTestBuilder extends ReflectionInstanceTestBuilder<Authority> {

    private String authority;

    private AuthorityTestBuilder() {}

    public static AuthorityTestBuilder anAuthority() {
        return new AuthorityTestBuilder();
    }

    @Override
    public void setFields(Authority instance) {
        setField(instance, "authority", authority);
    }

    public static Authority authority(String authority) {
        return anAuthority().withAuthority(authority).build();
    }

    public AuthorityTestBuilder withAuthority(String authority) {
        this.authority = authority;
        return this;
    }
}