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

import com.ixortalk.test.builder.ReflectionInstanceTestBuilder;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class UserProfileTestBuilder extends ReflectionInstanceTestBuilder<UserProfile> {

    private String name;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private Set<Authority> authorities = newHashSet();
    private LoginProvider loginProvider;

    private UserProfileTestBuilder() {}

    public static UserProfileTestBuilder aUserProfile() {
        return new UserProfileTestBuilder();
    }

    @Override
    public void setFields(UserProfile instance) {
        setField(instance, "name", name);
        setField(instance, "email", email);
        setField(instance, "firstName", firstName);
        setField(instance, "lastName", lastName);
        setField(instance, "profilePictureUrl", profilePictureUrl);
        setField(instance, "authorities", authorities);
        setField(instance, "loginProvider", loginProvider);
    }

    public UserProfileTestBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public UserProfileTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserProfileTestBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserProfileTestBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserProfileTestBuilder withProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
        return this;
    }

    public UserProfileTestBuilder withAuthorities(Authority... authorities) {
        this.authorities.addAll(newHashSet(authorities));
        return this;
    }

    public UserProfileTestBuilder withLoginProvider(LoginProvider loginProvider) {
        this.loginProvider = loginProvider;
        return this;
    }
}