package com.ixortalk.authorization.server.rest;

import com.ixortalk.authorization.server.AbstractSpringIntegrationTest;
import org.junit.Test;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import static com.ixortalk.authorization.server.TestConfigConstants.TEST_CLIENT_NAME;
import static com.ixortalk.test.oauth2.OAuth2TestTokens.getAccessToken;
import static com.jayway.restassured.RestAssured.given;
import static java.net.HttpURLConnection.HTTP_OK;

public class UserInfoControllerIntegrationTest extends AbstractSpringIntegrationTest {

    @Test
    public void getUserInfo() {
        given()
                .auth().preemptive().oauth2(testClientAccessToken().getValue())
                .when()
                .get("/user")
                .then()
                .statusCode(HTTP_OK);
    }

    private OAuth2AccessToken testClientAccessToken() {
        return getAccessToken(
                ixorTalkConfigProperties.getSecurity().getAuthentication().getOauthClients().get(TEST_CLIENT_NAME.configValue()).getClientid(),
                ixorTalkConfigProperties.getSecurity().getAuthentication().getOauthClients().get(TEST_CLIENT_NAME.configValue()).getSecret());
    }
}