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
import org.junit.Test;

import static com.ixortalk.authorization.server.TestConfigConstants.THIRD_PARTY_LOGIN_EVENTBRITE;
import static com.ixortalk.authorization.server.TestConfigConstants.THIRD_PARTY_LOGIN_IXORTALK;
import static com.ixortalk.authorization.server.TestConfigConstants.THIRD_PARTY_LOGIN_SALTO;
import static com.ixortalk.authorization.server.domain.AuthorityTestBuilder.authority;
import static com.ixortalk.authorization.server.domain.LoginProvider.IXORTALK;
import static com.ixortalk.authorization.server.domain.UserProfileTestBuilder.aUserProfile;
import static com.jayway.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static wiremock.org.apache.http.HttpHeaders.LOCATION;
import static wiremock.org.eclipse.jetty.http.HttpStatus.MOVED_TEMPORARILY_302;

public class LoginIntegrationTest extends AbstractSpringIntegrationTest {

    @Test
    public void loginRedirect() {
        String location =
                given()
                        .when()
                        .get("/hello")
                        .then()
                        .statusCode(MOVED_TEMPORARILY_302)
                        .extract().header(LOCATION);

        assertThat(location).isEqualTo(getLocalURL() + ixorTalkConfigProperties.getSecurity().getLoginUrl());
    }

    @Test
    public void login_withIxorTalk() {

        given()
                .filter(sessionFilter)
                .when()
                .get("/hello");

        String response =
                performOAuth2Login(THIRD_PARTY_LOGIN_IXORTALK)
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().asString();

        assertThat(response).isEqualTo("hello " + PRINCIPAL_NAME_IXORTALK);
    }

    @Test
    public void login_withEventbrite() {

        given()
                .filter(sessionFilter)
                .when()
                .get("/hello");

        String response =
                performOAuth2Login(THIRD_PARTY_LOGIN_EVENTBRITE)
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().asString();

        assertThat(response).isEqualTo("hello " + PRINCIPAL_NAME_EVENTBRITE);
    }

    @Test
    public void login_withSalto() {

        given()
                .filter(sessionFilter)
                .when()
                .get("/hello");

        String response =
                performOAuth2Login(THIRD_PARTY_LOGIN_SALTO)
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().asString();

        assertThat(response).isEqualTo("hello " + PRINCIPAL_NAME_SALTO);
    }

    @Test
    public void userProfileSaved() {
        given()
                .filter(sessionFilter)
                .when()
                .get("/hello");

        performOAuth2Login(THIRD_PARTY_LOGIN_IXORTALK)
                .then()
                .statusCode(HTTP_OK);

        assertThat(userProfileRestResource.findByEmail(PRINCIPAL_NAME_IXORTALK))
                .isPresent()
                .usingValueComparator(new IgnoringFieldsComparator("id"))
                .contains(
                        aUserProfile()
                                .withName(PRINCIPAL_NAME_IXORTALK)
                                .withEmail(PRINCIPAL_NAME_IXORTALK)
                                .withFirstName(FIRST_NAME_IXORTALK_PRINCIPAL)
                                .withLastName(LAST_NAME_IXORTALK_PRINCIPAL)
                                .withProfilePictureUrl(PROFILE_PICTURE_URL_IXORTALK_PRINCIPAL)
                                .withAuthorities(authority(ROLE_IXORTALK_ROLE_1), authority(ROLE_IXORTALK_ROLE_2))
                                .withLoginProvider(IXORTALK)
                                .build());
    }
}

