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
package com.ixortalk.authorization.server.config;

import com.ixortalk.authorization.server.AbstractSpringIntegrationTest;
import org.junit.Test;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import static com.ixortalk.authorization.server.TestConfigConstants.ADMIN_CLIENT_NAME;
import static com.jayway.restassured.RestAssured.given;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;

public class OAuth2ClientsIntegrationTest extends AbstractSpringIntegrationTest {

    @Test
    public void retrieveTokenForConfiguredClient() {
        OAuth2AccessToken oAuth2AccessToken =
                given()
                        .parameters("grant_type", "client_credentials")
                        .auth()
                        .preemptive()
                        .basic(
                                ixorTalkConfigProperties.getSecurity().getAuthentication().getOauthClients().get(ADMIN_CLIENT_NAME.configValue()).getClientid(),
                                ixorTalkConfigProperties.getSecurity().getAuthentication().getOauthClients().get(ADMIN_CLIENT_NAME.configValue()).getSecret())
                        .when()
                        .post("/oauth/token")
                        .then().statusCode(SC_OK)
                        .extract().as(OAuth2AccessToken.class);

        assertThat(oAuth2AccessToken.getValue()).isNotBlank();
    }
}

