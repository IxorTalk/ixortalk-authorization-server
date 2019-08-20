/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-present IxorTalk CVBA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ixortalk.authorization.server.domain;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import java.io.Serializable;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.EAGER;

@Entity
public class UserProfile implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String name;

    @Column
    private String email;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String profilePictureUrl;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "authorities", joinColumns = @JoinColumn(name = "user_profile_id"))
    private Set<Authority> authorities = newHashSet();

    @Column
    @Enumerated(STRING)
    private LoginProvider loginProvider;

    private UserProfile() {}

    public UserProfile(
            String name,
            String email,
            String firstName,
            String lastName,
            String profilePictureUrl,
            Set<Authority> authorities,
            LoginProvider loginProvider) {
        this.name = name;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePictureUrl = profilePictureUrl;
        this.authorities = authorities;
        this.loginProvider = loginProvider;
    }

    public UserProfile update(
            String name,
            String email,
            String firstName,
            String lastName,
            String profilePictureUrl,
            Set<Authority> authorities,
            LoginProvider loginProvider) {
        this.name = name;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePictureUrl = profilePictureUrl;
        this.authorities = authorities;
        this.loginProvider = loginProvider;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public LoginProvider getLoginProvider() {
        return loginProvider;
    }

    public UserProfile assertCorrectProvider(LoginProvider loginProvider) {
        if (this.loginProvider != loginProvider) {
            throw new ProfileConflictException("Login failed because a duplicate profile was detected for " + getName() + ": " + this.loginProvider + " & " + loginProvider);
        }
        return this;
    }
}

