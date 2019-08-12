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
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toSet;

public class ThirdPartyLoginProviders {

    private final Map<LoginProvider, ThirdPartyLoginProvider> thirdPartyLoginProviderMap;

    private Set<String> thirdPartyClientIds;

    @PostConstruct
    public void postConstruct() {
        thirdPartyClientIds = thirdPartyLoginProviderMap.values().stream().map(ThirdPartyLoginProvider::getResource).map(OAuth2ProtectedResourceDetails::getClientId).collect(toSet());
    }

    public ThirdPartyLoginProviders(List<ThirdPartyLoginProvider> thirdPartyLoginProviders) {
        thirdPartyLoginProviderMap = thirdPartyLoginProviders.stream().collect(Collectors.toMap(ThirdPartyLoginProvider::getLoginProvider, identity()));
    }

    public ThirdPartyLoginProvider getLoginProvider(LoginProvider loginProvider) {
        return thirdPartyLoginProviderMap.get(loginProvider);
    }

    public Collection<ThirdPartyLoginProvider> getLoginProviders() {
        return this.thirdPartyLoginProviderMap.values();
    }

    public Set<String> getThirdPartyClientIds() {
        return this.thirdPartyClientIds;
    }
}
