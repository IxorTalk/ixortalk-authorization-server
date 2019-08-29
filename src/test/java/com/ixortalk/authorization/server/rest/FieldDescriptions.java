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
package com.ixortalk.authorization.server.rest;

import com.ixortalk.authorization.server.domain.LoginProvider;
import org.springframework.restdocs.payload.FieldDescriptor;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.springframework.restdocs.payload.JsonFieldType.ARRAY;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

class FieldDescriptions {


    static class UserProfileFields {

        static final FieldDescriptor NAME = fieldWithPath("name").type(STRING).description("The principal name, being used as key identifier for this user.");
        static final FieldDescriptor EMAIL = fieldWithPath("email").type(STRING).description("The user's email address.");
        static final FieldDescriptor FIRST_NAME = fieldWithPath("firstName").type(STRING).description("The user's first name");
        static final FieldDescriptor LAST_NAME = fieldWithPath("lastName").type(STRING).description("The user's last name");
        static final FieldDescriptor PROFILE_PICTURE_URL = fieldWithPath("profilePictureUrl").type(STRING).description("The profile picture URL for this user.");
        static final FieldDescriptor AUTHORITIES = fieldWithPath("authorities").type(ARRAY).description("The user's list of roles/authorities.");
        static final FieldDescriptor LOGIN_PROVIDER = fieldWithPath("loginProvider").type(STRING).description("The login provider through which the user logged in, one of: " + listEnumValues(LoginProvider.class) + ".");
    }

    private static <T extends Enum> String listEnumValues(Class<T> enumClass) {
        return stream(enumClass.getEnumConstants()).map(Enum::name).collect(joining(", ", "[", "]"));
    }
}
