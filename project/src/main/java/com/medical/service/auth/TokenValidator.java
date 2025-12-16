package com.medical.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Minimal token validator. Replace with real JWT/SSO logic when available.
 */
@Service
public class TokenValidator {

    @Value("${auth.shared-secret:}")
    private String sharedSecret;

    public boolean isValid(String token) {
        if (!StringUtils.hasText(sharedSecret)) {
            // If no secret configured, consider validation disabled.
            return true;
        }
        return StringUtils.hasText(token) && sharedSecret.equals(token.trim());
    }
}
