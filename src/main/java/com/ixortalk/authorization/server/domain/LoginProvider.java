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
package com.ixortalk.authorization.server.domain;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

import java.util.List;
import java.util.Map;

public enum LoginProvider {

    IXORTALK {
        @Override
        public String getPrincipalName(Map<String, Object> map) {
            return (String) map.get("name");
        }

        @Override
        public String getFirstName(Map<String, Object> map) {
            return getUserInfoField(map, "firstName");
        }

        @Override
        public String getLastName(Map<String, Object> map) {
            return getUserInfoField(map, "lastName");
        }

        @Override
        public String getProfilePictureUrl(Map<String, Object> map, OAuth2RestTemplate userInfoProviderRestTemplate) {
            return getUserInfoField(map, "profilePictureUrl");
        }

        @Override
        public Object getUserInfo(Map<String, Object> map) {
            return map.get("userInfo");
        }

        private String getUserInfoField(Map<String, Object> map, String fieldName) {
            Map<String, Object> userInfo = (Map<String, Object>) getUserInfo(map);
            return (String) userInfo.get(fieldName);
        }
    },

    EVENTBRITE {
        @Override
        public String getPrincipalName(Map<String, Object> map) {
            List<Map<String, Object>> emails = (List<Map<String, Object>>) map.get("emails");
            return (String) emails.get(0).get("email");
        }

        @Override
        public String getFirstName(Map<String, Object> map) {
            return (String) map.get("first_name");
        }

        @Override
        public String getLastName(Map<String, Object> map) {
            return (String) map.get("last_name");
        }

        @Override
        public String getProfilePictureUrl(Map<String, Object> map, OAuth2RestTemplate userInfoProviderRestTemplate) {
            // TODO : Proper profile picture handing: https://github.com/IxorTalk/ixortalk-authorization-server/issues/6
            try {
                Object imageId = map.get("image_id");
                if (imageId != null) {
                    ResponseEntity<JsonNode> profilePictureResponse = userInfoProviderRestTemplate.getForEntity("https://www.eventbriteapi.com/v3/media/" + imageId, JsonNode.class);
                    return profilePictureResponse.getBody().get("url").textValue();
                }
            } catch (Exception e) {
                LOGGER.warn("Error retrieving profile pic from Eventbrite: " + e.getMessage(), e);
            }
            return null;
        }

        @Override
        public Object getUserInfo(Map<String, Object> map) {
            return map;
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginProvider.class);

    public abstract String getPrincipalName(Map<String, Object> map);

    public abstract String getFirstName(Map<String, Object> map);

    public abstract String getLastName(Map<String, Object> map);

    public abstract String getProfilePictureUrl(Map<String, Object> map, OAuth2RestTemplate userInfoProviderRestTemplate);

    public abstract Object getUserInfo(Map<String, Object> map);

}
