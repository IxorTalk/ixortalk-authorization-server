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

import org.junit.Test;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;
import static org.assertj.core.api.Assertions.assertThat;

public class UserProfileRestResourcesAnnotationTest {

    @Test
    public void allExportedMethodsShouldBeSecured() {
        assertThat(
                getAllInterfaces(UserProfileRestResource.class)
                        .stream()
                        .map(Class::getMethods)
                        .flatMap(Arrays::stream)
                        .filter(method -> getDeclaredMethod(method, UserProfileRestResource.class).map(this::exportedAndUnsecured).orElse(true))
                        .collect(toList()))
                .describedAs("Exported, unsecured Spring Data REST method found!")
                .isEmpty();
    }

    private boolean exportedAndUnsecured(Method declaredMethod) {
        return declaredMethod.getAnnotation(PreAuthorize.class) == null && ofNullable(declaredMethod.getAnnotation(RestResource.class)).map(RestResource::exported).orElse(true);
    }

    private Optional<Method> getDeclaredMethod(Method method, Class<?> clazz) {
        try {
            return of(clazz.getDeclaredMethod(method.getName(), method.getParameterTypes()));
        } catch (NoSuchMethodException e) {
            return empty();
        }
    }
}
