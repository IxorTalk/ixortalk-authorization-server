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
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Test;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.net.URISyntaxException;

import static com.ixortalk.authorization.server.TestConfigConstants.TEST_CLIENT_ID;
import static com.ixortalk.authorization.server.TestConfigConstants.TEST_CLIENT_REDIRECT_URI;
import static com.ixortalk.authorization.server.TestConfigConstants.TEST_CLIENT_SECRET;
import static com.ixortalk.authorization.server.TestConfigConstants.THIRD_PARTY_LOGIN_IXORTALK;
import static com.ixortalk.test.util.Randomizer.nextString;
import static com.jayway.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static wiremock.org.apache.http.HttpHeaders.LOCATION;
import static wiremock.org.eclipse.jetty.http.HttpStatus.MOVED_TEMPORARILY_302;

public class TokenIntegrationTest extends AbstractSpringIntegrationTest {

    @Test
    public void getAccessToken() throws URISyntaxException {
        given()
                .filter(sessionFilter)
                .urlEncodingEnabled(false)
                .parameters("client_id", TEST_CLIENT_ID.configValue())
                .parameters("redirect_uri", TEST_CLIENT_REDIRECT_URI.configValue())
                .parameters("response_type", "code")
                .parameters("state", nextString("state-"))
                .when()
                .get("/oauth/authorize");

        String location =
                performOAuth2Login(THIRD_PARTY_LOGIN_IXORTALK)
                        .then()
                        .statusCode(MOVED_TEMPORARILY_302)
                        .extract().header(LOCATION);

        OAuth2AccessToken oAuth2AccessToken =
                given()
                        .filter(sessionFilter)
                        .auth().preemptive().basic(TEST_CLIENT_ID.configValue(), TEST_CLIENT_SECRET.configValue())
                        .parameters("grant_type", "authorization_code")
                        .parameters("code", extractAuthorizationCodeFromRedirect(location))
                        .parameters("redirect_uri", TEST_CLIENT_REDIRECT_URI.configValue())
                        .when()
                        .post("/oauth/token")
                        .then()
                        .statusCode(HTTP_OK)
                        .extract().as(OAuth2AccessToken.class);

        assertThat(oAuth2AccessToken.getValue()).isNotBlank();
        assertThat(oAuth2AccessToken.getRefreshToken()).isNotNull();

    }

    private static String extractAuthorizationCodeFromRedirect(String location) throws URISyntaxException {
        return new URIBuilder(location)
                .getQueryParams()
                .stream()
                .filter(nameValuePair -> nameValuePair.getName().equals("code"))
                .findFirst()
                .map(NameValuePair::getValue)
                .orElseThrow(() -> new IllegalStateException("A code should be available"));
    }
}

