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

import com.ixortalk.authorization.server.AbstractSpringIntegrationTest;
import org.assertj.core.internal.IgnoringFieldsComparator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.inject.Inject;

import static com.ixortalk.authorization.server.domain.LoginProvider.EVENTBRITE;
import static com.ixortalk.authorization.server.domain.LoginProvider.IXORTALK;
import static com.ixortalk.authorization.server.domain.UserProfileTestBuilder.aUserProfile;
import static com.ixortalk.test.util.Randomizer.nextString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class AuthenticationSuccessEventListenerIntegrationTest extends AbstractSpringIntegrationTest {

    private static final String PRINCIPAL_EMAIL = nextString("user@organization.com");
    private static final String FIRST_NAME = nextString("firstName");
    private static final String LAST_NAME = nextString("lastName");
    private static final String PROFILE_PICTURE_URL = nextString("http://profile-pics.com/pic-");
    private static final String USER_INFO_OBJECT = nextString("userInfoObject");

    @Inject
    private AuthenticationSuccessEventListener authenticationSuccessEventListener;

    @Mock
    private AuthenticationSuccessEvent authenticationSuccessEvent;

    @Mock
    private OAuth2Authentication oAuth2Authentication;

    @Before
    public void before() {
        when(authenticationSuccessEvent.getAuthentication()).thenReturn(oAuth2Authentication);
    }

    @Test
    public void userDoesNotExist() {
        when(oAuth2Authentication.getPrincipal()).thenReturn(new IxorTalkPrincipal(IXORTALK, PRINCIPAL_EMAIL, PRINCIPAL_EMAIL, FIRST_NAME, LAST_NAME, PROFILE_PICTURE_URL, USER_INFO_OBJECT));

        authenticationSuccessEventListener.onApplicationEvent(authenticationSuccessEvent);

        assertThat(userProfileRestResource.findByEmail(PRINCIPAL_EMAIL))
                .isPresent()
                .usingValueComparator(new IgnoringFieldsComparator("id"))
                .contains(
                        aUserProfile()
                                .withName(PRINCIPAL_EMAIL)
                                .withEmail(PRINCIPAL_EMAIL)
                                .withFirstName(FIRST_NAME)
                                .withLastName(LAST_NAME)
                                .withLoginProvider(IXORTALK)
                                .build());
    }

    @Test
    public void userExists_sameLoginProvider() {

        userProfileRestResource.save(
                aUserProfile()
                        .withName(PRINCIPAL_EMAIL)
                        .withEmail(PRINCIPAL_EMAIL)
                        .withFirstName(FIRST_NAME)
                        .withLastName(LAST_NAME)
                        .withLoginProvider(IXORTALK)
                        .build());

        when(oAuth2Authentication.getPrincipal()).thenReturn(new IxorTalkPrincipal(IXORTALK, PRINCIPAL_EMAIL, PRINCIPAL_EMAIL, FIRST_NAME, LAST_NAME, PROFILE_PICTURE_URL, USER_INFO_OBJECT));

        authenticationSuccessEventListener.onApplicationEvent(authenticationSuccessEvent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void userExists_differentLoginProvider() {

        userProfileRestResource.save(
                aUserProfile()
                        .withName(PRINCIPAL_EMAIL)
                        .withEmail(PRINCIPAL_EMAIL)
                        .withFirstName(FIRST_NAME)
                        .withLastName(LAST_NAME)
                        .withLoginProvider(EVENTBRITE)
                        .build());

        when(oAuth2Authentication.getPrincipal()).thenReturn(new IxorTalkPrincipal(IXORTALK, PRINCIPAL_EMAIL, PRINCIPAL_EMAIL, FIRST_NAME, LAST_NAME, PROFILE_PICTURE_URL, USER_INFO_OBJECT));

        authenticationSuccessEventListener.onApplicationEvent(authenticationSuccessEvent);
    }
}

