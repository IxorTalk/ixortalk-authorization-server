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

import java.util.List;
import java.util.Map;

public enum LoginProvider {

    INTERNAL {
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
        public Object getUserInfo(Map<String, Object> map) {
            return map;
        }
    };

    public abstract String getPrincipalName(Map<String, Object> map);

    public abstract String getFirstName(Map<String, Object> map);

    public abstract String getLastName(Map<String, Object> map);

    public abstract Object getUserInfo(Map<String, Object> map);
}
