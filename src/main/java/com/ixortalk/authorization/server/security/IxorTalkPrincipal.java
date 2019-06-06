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
package com.ixortalk.authorization.server.security;

import com.ixortalk.authorization.server.domain.LoginProvider;

import java.io.Serializable;
import java.security.Principal;

public class IxorTalkPrincipal implements Principal, Serializable {

    private LoginProvider loginProvider;
    private String name;
    private String firstName;
    private String lastName;
    private Object userInfo;

    public IxorTalkPrincipal(LoginProvider loginProvider, String name, String firstName, String lastName, Object userInfo) {
        this.loginProvider = loginProvider;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userInfo = userInfo;
    }

    @Override
    public String getName() {
        return name;
    }

    public LoginProvider getLoginProvider() {
        return loginProvider;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Object getUserInfo() {
        return userInfo;
    }
}
