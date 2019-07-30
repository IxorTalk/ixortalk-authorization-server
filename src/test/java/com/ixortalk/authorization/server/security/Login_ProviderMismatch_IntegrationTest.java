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
import com.jayway.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static com.ixortalk.authorization.server.TestConfigConstants.THIRD_PARTY_LOGIN_IXORTALK;
import static com.ixortalk.authorization.server.domain.LoginProvider.EVENTBRITE;
import static com.ixortalk.authorization.server.domain.UserProfileTestBuilder.aUserProfile;
import static com.ixortalk.test.util.Randomizer.nextString;
import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.oauth2.common.util.OAuth2Utils.REDIRECT_URI;
import static wiremock.org.apache.http.HttpHeaders.LOCATION;
import static wiremock.org.eclipse.jetty.http.HttpStatus.MOVED_TEMPORARILY_302;
import static wiremock.org.eclipse.jetty.http.HttpStatus.UNAUTHORIZED_401;

public class Login_ProviderMismatch_IntegrationTest extends AbstractSpringIntegrationTest {

    public static final String INITIAL_REDIRECT_URI = "theInitialRedirectUri";
    public static final String INITIAL_REQUEST_PATH = "/initial-request";

    @Before
    public void before() {
        userProfileRestResource.save(
                aUserProfile()
                        .withName(PRINCIPAL_NAME_IXORTALK)
                        .withEmail(PRINCIPAL_NAME_IXORTALK)
                        .withFirstName(nextString("persistedFirstName"))
                        .withLastName(nextString("persistedLastName"))
                        .withProfilePictureUrl(nextString("persistedProfilePictureUrl"))
                        .withLoginProvider(EVENTBRITE)
                        .build());
    }

    private Response loginWithMismatchedProvider() {
        String location =
                given()
                        .filter(sessionFilter)
                        .when()
                        .get(ixorTalkConfigProperties.getThirdPartyLogins().get(THIRD_PARTY_LOGIN_IXORTALK.configValue()).getLoginPath())
                        .then()
                        .statusCode(MOVED_TEMPORARILY_302)
                        .extract().header(LOCATION);

        location =
                given()
                        .filter(sessionFilter)
                        .when()
                        .get(location)
                        .then()
                        .statusCode(MOVED_TEMPORARILY_302)
                        .extract().header(LOCATION);

        return given()
                .filter(sessionFilter)
                .when()
                .get(location);
    }

    @Test
    public void returns401() {
        loginWithMismatchedProvider()
                .then()
                .statusCode(UNAUTHORIZED_401);
    }

    @Test
    public void retry() {

        given()
                .filter(sessionFilter)
                .when()
                .queryParam(REDIRECT_URI, INITIAL_REDIRECT_URI)
                .get(INITIAL_REQUEST_PATH);

        loginWithMismatchedProvider();

        String retryLoginRedirect =
                given()
                        .filter(sessionFilter)
                        .when()
                        .get("/retry-login")
                        .then()
                        .statusCode(MOVED_TEMPORARILY_302)
                        .extract().header(LOCATION);

        assertThat(retryLoginRedirect).endsWith(INITIAL_REQUEST_PATH + "?" + REDIRECT_URI + "=" + INITIAL_REDIRECT_URI);

    }

    @Test
    public void retry_noInitialRequest() {

        loginWithMismatchedProvider();

        String retryLoginRedirect =
                given()
                        .filter(sessionFilter)
                        .when()
                        .get("/retry-login")
                        .then()
                        .statusCode(MOVED_TEMPORARILY_302)
                        .extract().header(LOCATION);

        assertThat(retryLoginRedirect).endsWith("/");
    }
}
