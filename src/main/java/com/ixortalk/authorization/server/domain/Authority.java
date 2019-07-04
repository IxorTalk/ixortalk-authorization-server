package com.ixortalk.authorization.server.domain;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Authority {

    @Column
    private String authority;

    private Authority() {}

    private Authority(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

    public static Authority authority(String authority) {
        return new Authority(authority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Authority authority = (Authority) o;
        return Objects.equal(this.authority, authority.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(authority);
    }

}
